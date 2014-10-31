package com.oraclecorp.ese.dto;

import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * @author mattulur
 *
 *This is an optimized version of Event.java
 *This DTO contains only the fields that are displayed in UI.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@XmlRootElement(name="ExtjsEvent")
@XmlAccessorType(XmlAccessType.NONE)
public class ExtjsEvent {
    @XmlElement(name="agentVersion")
    private String agentVersion;
   
    @XmlElement(name="hostName")
    private String hostName;
    
    @XmlElement(name="eventData")
    private Map<String,String> eventData = new HashMap<String,String>();

    public String getAgentVersion() {
        return agentVersion;
    }

    public void setAgentVersion(String agentVersion) {
        this.agentVersion = agentVersion;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public Map<String, String> getEventData() {
        return eventData;
    }

    public void setEventData(Map<String, String> eventData) {
        this.eventData = eventData;
    }    
    
    public String toString() {
        StringBuilder strBuilder = new StringBuilder();        
        for (String key : eventData.keySet()) {
           strBuilder.append(key + " : " + eventData.get(key));
        }
        
        return strBuilder.toString();
    }
    
    public String getEventData(String key) {
        return eventData.get(key);
    }
    
}