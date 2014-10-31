package com.oraclecorp.ese.dto;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
/**
 * @author mattulur
 * 
 * Event DTO which can be mapped to each line JSON in log files
 *
 */
@XmlRootElement(name="UIEvent")
@XmlAccessorType(XmlAccessType.NONE)
public class Event {
        
    @XmlElement(name="time")
    private Timestamp time;
    
    @XmlElement(name="agentName")
    private String agentName;
    
    @XmlElement(name="agentVersion")
    private String agentVersion;
    
    @XmlElement(name="type")
    private String type;
    
    @XmlElement(name="hostName")
    private String hostName;
    
    @XmlElement(name="ipAddress")
    private String ipAddress;
    
    @XmlElement(name="userName")
    private String userName;
    
    @XmlElement(name="osName")
    private String osName;
    
    @XmlElement(name="osVersion")
    private String osVersion;
    
    @XmlElement(name="osArchitecture")
    private String osArchitecture;
    
    @XmlElement(name="eventData")
    private Map<String,String> eventData = new HashMap<String,String>();

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public String getAgentVersion() {
        return agentVersion;
    }

    public void setAgentVersion(String agentVersion) {
        this.agentVersion = agentVersion;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getOsName() {
        return osName;
    }

    public void setOsName(String osName) {
        this.osName = osName;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public String getOsArchitecture() {
        return osArchitecture;
    }

    public void setOsArchitecture(String osArchitecture) {
        this.osArchitecture = osArchitecture;
    }

    public Map<String, String> getEventData() {
        return eventData;
    }

    public void setEventData(Map<String, String> eventData) {
        this.eventData = eventData;
    }    
    
    public String toString() {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("Type : " + type)
                .append("Time : " + time )
                .append("Agent : " + agentName);
        for (String key : eventData.keySet()) {
           strBuilder.append(key + " : " + eventData.get(key));
        }
        
        return strBuilder.toString();
    }
    
    public String getEventData(String key) {
        return eventData.get(key);
    }
    
}