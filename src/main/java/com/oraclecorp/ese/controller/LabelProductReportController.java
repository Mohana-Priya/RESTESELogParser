package com.oraclecorp.ese.controller;
 
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.GZIPInputStream;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import com.oraclecorp.ese.dto.Event;
import com.oraclecorp.ese.dto.ExtjsEvent;
import com.oraclecorp.ese.util.CommandExecutor;

@Controller
@RequestMapping("/logparser")
public class LabelProductReportController {

	private final SimpleDateFormat logFileNameDateFormat = new SimpleDateFormat("yyyy/MM/'sei-events.'yyyy-MM-dd'.log.gz'");
	private final String baseDirLocation = System.getenv().get("ESELogParserBaseDir");
	
	@RequestMapping(value="/label-product-report", method = RequestMethod.GET)
	public @ResponseBody List<Event> getDeafaultLabelProductReport() { 				
		BufferedReader reader = null;
		List<Event> eventsList = new ArrayList<Event>();
		try {
			GZIPInputStream gzstream = new GZIPInputStream(new FileInputStream(baseDirLocation+logFileNameDateFormat.format(new Date())));
			reader = new BufferedReader(new InputStreamReader(gzstream));
			addJsonsFromFile(reader,eventsList);			
		} catch(Exception e) {
			e.printStackTrace();			
		} finally {
			if(reader != null) {
				try {
					reader.close();
				} catch(IOException e){}
			}
		}
		return eventsList;
		//Spring uses InternalResourceViewResolver and return back index.jsp
		//return "index";
 
	}
		
	@RequestMapping(value="/label-product-report/{startDateString}/{endDateString}", method = RequestMethod.GET)
	public @ResponseBody List<Event> getLabelProductReport(@PathVariable String startDateString,@PathVariable String endDateString) { 		
		BufferedReader reader = null;		
		List<Event> eventsList = new ArrayList<Event>();
		 
		try
		{			
			SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");			
			Date startDate = df.parse(startDateString);
			Date endDate = df.parse(endDateString);
			
			/*
			 *Identifying all the log files to be read for the given date range 
			 *TODO need to consider .log file instead of .gz if today's date lies in the range*/
			long interval = 24*60*60*1000;
			List<String> fileNames = new ArrayList<String>();			
			long startDateTime = startDate.getTime();
			long endDateTime = endDate.getTime();
			while (startDateTime <= endDateTime) {
			    fileNames.add(baseDirLocation+logFileNameDateFormat.format(new Date(startDateTime)));
			    startDateTime += interval;
			}
			
			for(String path: fileNames){
				GZIPInputStream gzstream = new GZIPInputStream(new FileInputStream(path));
				reader = new BufferedReader(new InputStreamReader(gzstream));			
				addJsonsFromFile(reader,eventsList);
				gzstream.close();
			}
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			if(reader != null) {
				try {					
					reader.close();
				}catch(IOException e){}
			}
		}
		return eventsList;
		//Spring uses InternalResourceViewResolver and return back index.jsp
		//return "index";
 
	}
	
	private void addJsonsFromFile(BufferedReader reader,List<Event> eventsList) throws JsonParseException, JsonMappingException, IOException{					
		String line = "";
		ObjectMapper mapper = new ObjectMapper();
		while((line = reader.readLine()) != null) {			
			Event event = mapper.readValue(line, Event.class);
			String labelName = event.getEventData("VIEW_LABEL");
			String cmd = event.getEventData("COMMAND");
			if (labelName == null || cmd == null || !cmd.equalsIgnoreCase("label_product") || labelName.split("_").length < 3){					
				continue;
			}				
			eventsList.add(event);
		}
	}
	
	@RequestMapping(value="/label_product_failures_report/{startValue}/{endValue}/{isTodayReport}", method = RequestMethod.GET)
	public @ResponseBody List<ExtjsEvent> getLabelProductFailuresReport(@PathVariable String startValue,@PathVariable String endValue,@PathVariable boolean isTodayReport) throws Exception {
		
		List<ExtjsEvent> ExtjsEventList = new ArrayList<ExtjsEvent>();

		try
		{
			if(isTodayReport){
				Integer startHour = Integer.parseInt(startValue);
				Integer endHour = Integer.parseInt(endValue);
				String fileName = baseDirLocation+logFileNameDateFormat.format(new Date());
				fileName = fileName.substring(0, fileName.lastIndexOf(".gz"));
				File f = new File(fileName);
				if(f.exists()){
					CommandExecutor.executeUnixCommand("grep '\\\"COMMAND\\\":\\\"label_product\\\"' "+fileName+"| grep '\\\"COMMAND_STATUS\\\":\\\"FAILED\\\"'  | grep '\\\"agentName\\\":\\\"ADE\\\"' |grep -v '\\\"VIEW_LABEL\\\":\\\"\\\"'",ExtjsEventList,startHour,endHour);					
				}
			} else {
				SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");			
				Date startDate = df.parse(startValue);
				Date endDate = df.parse(endValue);
				
				/*
				 *Identifying all the log files to be read for the given date range */
				long interval = 24*60*60*1000;
				StringBuffer sb = new StringBuffer("");			
				long startDateTime = startDate.getTime();
				long endDateTime = endDate.getTime();
				while (startDateTime <= endDateTime) {
					String fileName = baseDirLocation+logFileNameDateFormat.format(new Date(startDateTime));
					File f = new File(fileName);
					if(f.exists())
						sb.append(" "+fileName);
				    startDateTime += interval;
				}
				
				if(!sb.toString().trim().equals("")){
					CommandExecutor.executeUnixCommand("zcat "+sb+" | grep '\\\"COMMAND\\\":\\\"label_product\\\"' | grep '\\\"COMMAND_STATUS\\\":\\\"FAILED\\\"'  | grep '\\\"agentName\\\":\\\"ADE\\\"' |grep -v '\\\"VIEW_LABEL\\\":\\\"\\\"'",ExtjsEventList);
				}
			}
			
		} catch(Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}
		
		return ExtjsEventList;
	}
	
	@RequestMapping(value="/getcurrentdatetime", method = RequestMethod.GET)
	public @ResponseBody String getCurrentDateTime() throws Exception {
		return new SimpleDateFormat("dd-MM-yyyy H:m:s").format(new Date());	
	}
}