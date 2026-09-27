package com.opsmemory.api.dto;

import java.util.List;

public class IncidentAnalyzeResponse {
    private String incident;
    private boolean memoryFound;
    private List<String> recalledMemories;
    private AnalysisDetails analysis;

    public String getIncident() { return incident; }
    public void setIncident(String incident) { this.incident = incident; }
    public boolean isMemoryFound() { return memoryFound; }
    public void setMemoryFound(boolean memoryFound) { this.memoryFound = memoryFound; }
    public List<String> getRecalledMemories() { return recalledMemories; }
    public void setRecalledMemories(List<String> recalledMemories) { this.recalledMemories = recalledMemories; }
    public AnalysisDetails getAnalysis() { return analysis; }
    public void setAnalysis(AnalysisDetails analysis) { this.analysis = analysis; }
}
