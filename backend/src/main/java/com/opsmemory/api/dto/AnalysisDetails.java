package com.opsmemory.api.dto;

import java.util.List;

public class AnalysisDetails {
    private String severity;
    private String likelyRootCause;
    private List<String> immediateActions;
    private String reasoning;
    private String historicalReference;

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getLikelyRootCause() { return likelyRootCause; }
    public void setLikelyRootCause(String likelyRootCause) { this.likelyRootCause = likelyRootCause; }
    public List<String> getImmediateActions() { return immediateActions; }
    public void setImmediateActions(List<String> immediateActions) { this.immediateActions = immediateActions; }
    public String getReasoning() { return reasoning; }
    public void setReasoning(String reasoning) { this.reasoning = reasoning; }
    public String getHistoricalReference() { return historicalReference; }
    public void setHistoricalReference(String historicalReference) { this.historicalReference = historicalReference; }
}
