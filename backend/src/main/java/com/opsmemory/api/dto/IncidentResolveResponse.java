package com.opsmemory.api.dto;

public class IncidentResolveResponse {
    private boolean success;
    private String documentId;
    private String message;

    public IncidentResolveResponse() {}

    public IncidentResolveResponse(boolean success, String documentId, String message) {
        this.success = success;
        this.documentId = documentId;
        this.message = message;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
