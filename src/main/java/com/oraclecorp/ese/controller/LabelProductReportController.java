package com.oraclecorp.ese.controller;
 
import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import com.oraclecorp.ese.dto.ExtjsEvent;
import com.oraclecorp.ese.util.CommandExecutor;
import com.oraclecorp.ese.util.DataCon;

@Controller
@RequestMapping("/logparser")
public class LabelProductReportController {

	public static final SimpleDateFormat REQUEST_PARAMETER_DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");
	private final SimpleDateFormat logFileNameDateFormat = new SimpleDateFormat("yyyy/MM/'sei-events.'yyyy-MM-dd'.log'");
	private final SimpleDateFormat gzLogFileNameDateFormat = new SimpleDateFormat("yyyy/MM/'sei-events.'yyyy-MM-dd'.log.gz'");
	private final String baseDirLocation = System.getenv().get("ESELogParserBaseDir");
	
	@RequestMapping(value="/label_product_failures_report/{startValue}/{endValue}/{isTodayReport}", method = RequestMethod.GET)
	public @ResponseBody List<ExtjsEvent> getLabelProductFailuresReport(@PathVariable String startValue,@PathVariable String endValue,@PathVariable boolean isTodayReport) throws Exception {
		
		List<ExtjsEvent> ExtjsEventList = new ArrayList<ExtjsEvent>();

		try
		{
			if(isTodayReport){
				Integer startHour = Integer.parseInt(startValue);
				Integer endHour = Integer.parseInt(endValue);
				String fileName = baseDirLocation+logFileNameDateFormat.format(new Date());				
				File f = new File(fileName);
				if(f.exists()){
					CommandExecutor.executeUnixCommand("grep '\\\"COMMAND\\\":\\\"label_product\\\"' "+fileName+"| grep '\\\"COMMAND_STATUS\\\":\\\"FAILED\\\"'  | grep '\\\"agentName\\\":\\\"ADE\\\"' |grep -v '\\\"VIEW_LABEL\\\":\\\"\\\"'",ExtjsEventList,startHour,endHour);					
				}
			} else {
				/*
				 * Calculating the min date to check in log files
				 * for any dates <min date we should check in DB
				 * For now we assume we maintain 6months old logs in log files prior to that records are maintained in DB*/
				Calendar cal = Calendar.getInstance();  //Get current date/month i.e 27 Feb, 2012
				cal.add(Calendar.MONTH, -6);   //Go to date, 6 months ago 27 July, 2011
				cal.set(Calendar.DAY_OF_MONTH, 1); //set date, to make it 1 July, 2011
				Date minLogDate = REQUEST_PARAMETER_DATE_FORMAT.parse(REQUEST_PARAMETER_DATE_FORMAT.format(cal.getTime()));//to truncate the time part in this date
				cal.add(Calendar.DAY_OF_MONTH, -1);
				Date previousDate = REQUEST_PARAMETER_DATE_FORMAT.parse(REQUEST_PARAMETER_DATE_FORMAT.format(cal.getTime()));
				
				SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");		
				Date startDate = df.parse(startValue);
				Date endDate = df.parse(endValue);
				
				if(startDate.compareTo(minLogDate) >= 0 && endDate.compareTo(minLogDate) >= 0){
					getLabelProductFailureEventsFromLogs(startDate,endDate,ExtjsEventList);
				} else if(startDate.compareTo(minLogDate) < 0 && endDate.compareTo(minLogDate) < 0){
					getLabelProductFailureEventsFromDB(startValue,endValue,ExtjsEventList);
				} else if(startDate.compareTo(minLogDate) < 0 && endDate.compareTo(minLogDate) >= 0){
					getLabelProductFailureEventsFromLogs(startDate,previousDate,ExtjsEventList);
					getLabelProductFailureEventsFromDB(REQUEST_PARAMETER_DATE_FORMAT.format(minLogDate),endValue,ExtjsEventList);
				}
								
			}
			
		} catch(Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}
		
		return ExtjsEventList;
	}
	
	private void getLabelProductFailureEventsFromDB(String startValue,String endValue, List<ExtjsEvent> extjsEventList) throws SQLException {
		Connection con = null;
		Statement st = null;
		ObjectMapper mapper = new ObjectMapper();
		try {			
			con = DataCon.getConnnection();
			st = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
			
			System.out.println("Query:----->  select AGENT_VERSION,HOST_NAME,EVENT_DATA from LABEL_PRODUCT_FAILURE_EVENTS where to_timestamp('"+startValue+"','dd-MM-yyyy')<= EVENT_TIME and trunc(EVENT_TIME)<=to_timestamp('"+endValue+"','dd-MM-yyyy')");			
			ResultSet rs=st.executeQuery("select AGENT_VERSION,HOST_NAME,EVENT_DATA from LABEL_PRODUCT_FAILURE_EVENTS where to_timestamp('"+startValue+"','dd-MM-yyyy')<= EVENT_TIME and trunc(EVENT_TIME)<=to_timestamp('"+endValue+"','dd-MM-yyyy')");

			while(rs.next()) {
				StringBuffer sb = new StringBuffer();
				sb.append("{");
				sb.append("\"agentVersion\":\""+rs.getString(1)+"\",");
				sb.append("\"hostName\":\""+rs.getString(2)+"\",");
				sb.append("\"eventData\":"+rs.getString(3));				
				sb.append("}");
				System.out.println(sb);
				ExtjsEvent event = mapper.readValue(sb.toString(), ExtjsEvent.class);
				extjsEventList.add(event);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(con!=null)
				con.close();
			if(st!=null)
				st.close();
		}		
	}

	private void getLabelProductFailureEventsFromLogs(Date startDate,Date endDate,List<ExtjsEvent> ExtjsEventList) throws Exception{		
		
		/*
		 *Identifying all the log files to be read for the given date range */
		long interval = 24*60*60*1000;
		StringBuffer sb = new StringBuffer("");			
		long startDateTime = startDate.getTime();
		long endDateTime = endDate.getTime();
		while (startDateTime <= endDateTime) {
			String fileName = baseDirLocation+gzLogFileNameDateFormat.format(new Date(startDateTime));
			File f = new File(fileName);
			if(f.exists()) {
				sb.append(" "+fileName);
			}
		    startDateTime += interval;
		}
		
		if(!sb.toString().trim().equals("")){
			CommandExecutor.executeUnixCommand("zcat "+sb+" | grep '\\\"COMMAND\\\":\\\"label_product\\\"' | grep '\\\"COMMAND_STATUS\\\":\\\"FAILED\\\"'  | grep '\\\"agentName\\\":\\\"ADE\\\"' |grep -v '\\\"VIEW_LABEL\\\":\\\"\\\"'",ExtjsEventList);
		}
		
		/*
		 *If current date is selected then we should search in .log file instead of .log.gz file */
		if(REQUEST_PARAMETER_DATE_FORMAT.format(endDate).equals(REQUEST_PARAMETER_DATE_FORMAT.format(new Date()))){
			String fileName = baseDirLocation+logFileNameDateFormat.format(new Date());				
			File f = new File(fileName);
			if(f.exists()){
				CommandExecutor.executeUnixCommand("grep '\\\"COMMAND\\\":\\\"label_product\\\"' "+fileName+"| grep '\\\"COMMAND_STATUS\\\":\\\"FAILED\\\"'  | grep '\\\"agentName\\\":\\\"ADE\\\"' |grep -v '\\\"VIEW_LABEL\\\":\\\"\\\"'",ExtjsEventList);					
			}
		}
	}
	
	@RequestMapping(value="/getcurrentdatetime", method = RequestMethod.GET)
	public @ResponseBody String getCurrentDateTime() throws Exception {
		return new SimpleDateFormat("dd-MM-yyyy H:m:s").format(new Date());	
	}
}