package com.opsmemory.client.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A single memory item to retain in Hindsight.
 * Maps to the MemoryItem schema in the Hindsight OpenAPI spec.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MemoryItem {

    private String content;
    private String context;

    @JsonProperty("document_id")
    private String documentId;

    private String timestamp;

    public MemoryItem() {}

    public MemoryItem(String content, String context, String documentId) {
        this.content = content;
        this.context = context;
        this.documentId = documentId;
    }

    public MemoryItem(String content, String context, String documentId, String timestamp) {
        this.content = content;
        this.context = context;
        this.documentId = documentId;
        this.timestamp = timestamp;
    }

    // Getters and setters

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getContext() { return context; }
    public void setContext(String context) { this.context = context; }

    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return "MemoryItem{documentId='" + documentId + "', context='" + context + "'}";
    }
}
