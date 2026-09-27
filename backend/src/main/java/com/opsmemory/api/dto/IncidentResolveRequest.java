package com.opsmemory.api.dto;

public class IncidentResolveRequest {
    private String incident;
    private String resolution;
    private String outcome;
    private String rootCause;

    public String getIncident() { return incident; }
    public void setIncident(String incident) { this.incident = incident; }
    public String getResolution() { return resolution; }
    public void setResolution(String resolution) { this.resolution = resolution; }
    public String getOutcome() { return outcome; }
    public void setOutcome(String outcome) { this.outcome = outcome; }
    public String getRootCause() { return rootCause; }
    public void setRootCause(String rootCause) { this.rootCause = rootCause; }
}
