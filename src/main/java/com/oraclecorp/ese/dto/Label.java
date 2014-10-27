package com.oraclecorp.ese.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Label {

    public static final int ENABLED = 0;
    public static final int PARTIALLY_ENABLED = 1;
    public static final int PUSH_FAILED = 2;
    public static final int STARTED = 3;
    
    private String name;
    // All this Label's events.
    private List<Event> events = new ArrayList<Event>();
    // Track per-site events.
    private Map<String, List<Event>> siteEvents = new HashMap<String, List<Event>>();
    private Map<String,SiteStatus> siteStatus = new HashMap<String, SiteStatus>();
    private int status = STARTED;
    private long timestamp = 0;
    private boolean finalStatus = false;

    public Label() {
    }

    public Label(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

    public void addEvent(Event event)
    {
        this.events.add(event);
    	String labelServer = event.getEventData("LABEL_SERVER_NAME");
		String labelServerVolume = event.getEventData("LABEL_SERVER_VOLUME");
		String pushVolume = labelServer + ":" + labelServerVolume;
		String op = event.getEventData("OPERATION");
		
		if(! op.toLowerCase().contains("push")){
			// Non push events are irrelevant to us.
			return;
		}
		
		if(! this.siteEvents.containsKey(pushVolume)){
			this.siteEvents.put(pushVolume, new ArrayList<Event>());
		}
		
		this.siteEvents.get(pushVolume).add(event);

    }

    public void calculateStatus(){
    	for(Event e: this.events){
    		this.updateStatus(e);
    	}
    	this.finalStatus = true;
    }
    
    public int[] getTotals(){
    	
    	int[] result = new int[]{0,0,0,0};
    	
    	for(String volume : this.siteStatus.keySet()){
    		result[this.siteStatus.get(volume).getPushStatus()]++;
    	}
    	return result;
    }
    
    public int getPushStatus(){
        return this.status;
    }
    
    public SiteStatus getSiteStatus(String volume){
    	return this.siteStatus.get(volume);
    }
    
    public long getTimeStamp(){
    	return this.timestamp;
    }
    
    public String getPushStatusString(String site){
    	
    	// REFACTOR
    	if(this.siteStatus.get(site).getPushStatus() == Label.ENABLED){
    		return "ENABLED";
    	}else if(this.siteStatus.get(site).getPushStatus()  == Label.PARTIALLY_ENABLED){
    		return "PARTIALLY_ENABLED";
    	}else if(this.siteStatus.get(site).getPushStatus()  == Label.STARTED){
        		return "STARTED";
    	}else{
    		return "PUSH_FAILED";
    	}
    }
    
    private void updateStatus(Event event){
    	
    	if(event.getType().equals("LABEL_INTG_EVENT"))
    	{
    		
    		String operation = event.getEventData("OPERATION");
    		String status = event.getEventData("OPERATION_STATUS");
    		String adeSite = event.getEventData("ADE_SITE");
    		this.timestamp = event.getTime().getTime();
    		
    		String labelServer = event.getEventData("LABEL_SERVER_NAME");
    		String labelServerVolume = event.getEventData("LABEL_SERVER_VOLUME");
    		
    		String pushVolume = labelServer + ":" + labelServerVolume;
    		
    	
    		if (! this.siteStatus.containsKey(pushVolume))
    		{
    			// create a new Site Status
    			this.siteStatus.put(pushVolume, new SiteStatus(adeSite, labelServer, labelServerVolume, Label.STARTED));
    		}
    		
    		this.siteStatus.get(pushVolume).setTimestamp(event.getTime().getTime());
    		
    		if(operation.toLowerCase().equals("initial_push") || operation.toLowerCase().equals("initialpush")){
    			if (status.equals("COMPLETED"))
    			{
    				// Initial Push completed. Label is partially Enabled
    				this.siteStatus.get(pushVolume).setPushStatus(Label.PARTIALLY_ENABLED);
    				return;
    			}
    			else if(status.equals("FAILED"))
    			{
    				// Initial Push Failed. Push failed.
    				this.siteStatus.get(pushVolume).setPushStatus(Label.PUSH_FAILED);
    				this.status = Label.PUSH_FAILED;
    				return;
    			}
    		}
    		
    		if(operation.toLowerCase().equals("final_push") 
    				|| operation.toLowerCase().equals("finalpush")
    				|| operation.toLowerCase().equals("auto_push"))
    		{
    			
    			if (status.equals("FAILED") && this.siteStatus.get(pushVolume).getPushStatus() != Label.PARTIALLY_ENABLED)
    			{
    				// Final Push Failed.
    				this.siteStatus.get(pushVolume).setPushStatus(Label.PUSH_FAILED);
    				this.status = Label.PUSH_FAILED;
    			}
    			else if(status.equals("COMPLETED") && this.siteStatus.get(pushVolume).getPushStatus() != Label.PUSH_FAILED)
    			{
    				// Final push completed. Label is good to go.
    				this.siteStatus.get(pushVolume).setPushStatus(Label.ENABLED);
    			}
    		}else{
    			// not a final push. So Any Failure means label push failed.
    			if (status.equals("FAILED") && operation.toLowerCase().contains("push")){
    				this.siteStatus.get(pushVolume).setPushStatus(Label.PUSH_FAILED);
    				this.status = Label.PUSH_FAILED; // This is a global identifier.
    			}
    		}
    		
    	}
    }

    public boolean hasPushFailures() {    	
    	if (!this.finalStatus){
    		this.calculateStatus(); // If the calling code hasn't called calculateStatus do it now.
    	}
    	return this.getFailedSites().size() > 0;
    }
    
    public boolean isPartiallyEnabled(){
    	return this.status != Label.PUSH_FAILED && this.hasPushFailures();
    }
    
    /***
     * Get the sites with push failures
     * @return List of Site Names with failures
     */
    public List<String> getFailedSites(){
    	
    	ArrayList<String> pushVolumes = new ArrayList<String>();
    	for(String volume : this.siteStatus.keySet()){
    		if(this.siteStatus.get(volume).getPushStatus() == Label.PUSH_FAILED || this.siteStatus.get(volume).getPushStatus() == Label.PARTIALLY_ENABLED){
    			pushVolumes.add(volume);
    		}
    	}
    	return pushVolumes;
    }

    /**
     * Return the push status for each volume.
     * @return Map pushVolume => pushStatus
     */
    public Map<String, String> getPushVolumeStatus() {
        Map<String, String> pushVolumesStatus = new HashMap<String, String>();
        for (Event event : events) {
            if (event.getType().equals("LABEL_INTG_EVENT"))
            {
            	
                String operation = event.getEventData().get("OPERATION");
                String status = event.getEventData().get("OPERATION_STATUS");
                String labelServer = event.getEventData().get("LABEL_SERVER_NAME");
                String labelServerVolume = event.getEventData().get("LABEL_SERVER_VOLUME");
                
                
                if (operation.toLowerCase().contains("push")
                        && labelServer != null && !"INIT".equalsIgnoreCase(status)) {
                 
                    String pushVolume = labelServer + ":" + labelServerVolume;
                    String pushStatus = event.getAgentName() + " " + operation + " " + status;

                    pushVolumesStatus.put(pushVolume, pushStatus);
                }
            }
        }
        return pushVolumesStatus;
    }

    
    
    public List<Event> getPushEvents(){
    	return this.getPushEvents(this.events);
    }
    
    public List<Event> getPushEvents(String pushVolume){
    	return this.getPushEvents(this.siteEvents.get(pushVolume));
    }
    
    private List<Event> getPushEvents(List<Event> eventList) {
        List<Event> pushEvents = new ArrayList<Event>();
        for (Event event : eventList) {
            if (event.getType().equals("LABEL_INTG_EVENT") || event.getType().equals("UIP_ALERT_EVENT")) {
                String operation = event.getEventData().get("OPERATION");
                if (operation != null && operation.toLowerCase().contains("push")) {
                    pushEvents.add(event);
                }
            }
        }
        return pushEvents;
    }
    
    

    public List<Event> getFailedOperationEvents() {
        List<Event> failedPushOpEvents = new ArrayList<Event>();

        Map<String,List<Event>> operationEvents = new HashMap<String,List<Event>>();
        for (Event event : events) {
            String operation = event.getEventData("OPERATION");
            if (operation != null) {
               String key = event.getAgentName() + ":" + operation;
               if (!operationEvents.containsKey(key)) {
                   operationEvents.put(key, new ArrayList<Event>());
               }
               operationEvents.get(key).add(event);
            }
        }

        for (Map.Entry<String,List<Event>> entry : operationEvents.entrySet()) {
            if (entry.getKey().toLowerCase().contains("push")) {
                boolean operationFailed = false;
                List<Event> opEvents = operationEvents.get(entry.getKey());
                Event lastEvent = opEvents.get(opEvents.size()-1);
                if ("FAILED".equalsIgnoreCase(
                    lastEvent.getEventData("OPERATION_STATUS"))) {
                    failedPushOpEvents.add(lastEvent);
                }
            }
        }
        for (Event event : events) {
            if (event.getType().equals("UIP_ALERT_EVENT")) {
                failedPushOpEvents.add(event);
            }
        }

        return failedPushOpEvents;
    }
}