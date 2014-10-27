package com.oraclecorp.ese.dto;

import java.util.List;
import java.util.Map;

/**
 * @author mattulur
 *
 */
public class FailedLabel {
    private String name;
    private Map<String,String> availability;
    private List<Event> failedOperations; 

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getAvailability() {
        return availability;
    }

    public void setAvailability(Map<String, String> availability) {
        this.availability = availability;
    }

    public void setFailedOperations(List<Event> failedOperations) {
        this.failedOperations = failedOperations;
    }

    public void addAvailability(String key, String value) {
        this.availability.put(key, value);
    }

    public List<Event> getFailedOperations() {
        return failedOperations;
    }

    public void addFailedOperations(Event failedOperation) {
        this.failedOperations.add(failedOperation);
    }
    
}
