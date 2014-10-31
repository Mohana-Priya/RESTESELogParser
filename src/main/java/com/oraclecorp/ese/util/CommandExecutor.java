package com.oraclecorp.ese.util;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.List;
import org.codehaus.jackson.map.ObjectMapper;
import com.oraclecorp.ese.dto.ExtjsEvent;

public class CommandExecutor {
	public static void executeUnixCommand(String cmd,List<ExtjsEvent> extjsEventList) throws Exception{
		System.out.println("Executing cmd:--> "+cmd);
		ObjectMapper mapper = new ObjectMapper();
		Process p;
		
		/*
		 * If the unix cmd contains | we can not directly run hence writing cmd to file first and then executing that file*/
		FileWriter fw = new FileWriter("/net/adc6140270/scratch/mattulur/IFarmMonitor/testscript.sh", false);
		fw.write(cmd);
		fw.close();
		p = Runtime.getRuntime().exec("/net/adc6140270/scratch/mattulur/IFarmMonitor/testscript.sh");
		
		
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
        BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        String s;
        /*
         * Json objects are separated by \n in src files*/
        while ((s=stdInput.readLine()) != null) {
        	ExtjsEvent event = mapper.readValue(s, ExtjsEvent.class);
        	extjsEventList.add(event);
        }
           
        while ((s = stdError.readLine()) != null) {
            throw new Exception(s);
        }		
	}

	public static void executeUnixCommand(String cmd,List<ExtjsEvent> extjsEventList, Integer startHour, Integer endHour) throws Exception {
		System.out.println("Executing cmd:--> "+cmd);
		ObjectMapper mapper = new ObjectMapper();
		Process p;
		
		FileWriter fw = new FileWriter("/net/adc6140270/scratch/mattulur/IFarmMonitor/testscript.sh", false);
		fw.write(cmd);
		fw.close();
		p = Runtime.getRuntime().exec("/net/adc6140270/scratch/mattulur/IFarmMonitor/testscript.sh");
				
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
        BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        String s;        
        while ((s=stdInput.readLine()) != null) {
        	ExtjsEvent event = mapper.readValue(s, ExtjsEvent.class);
        	int eventStartTime = Integer.parseInt(event.getEventData().get("START_TIME").split("-")[1].split(":")[0]);
        	if(startHour <= eventStartTime && eventStartTime <= endHour){
        		extjsEventList.add(event);
        	}        	
        }
           
        while ((s = stdError.readLine()) != null) {
            throw new Exception(s);
        }		
	}
}

