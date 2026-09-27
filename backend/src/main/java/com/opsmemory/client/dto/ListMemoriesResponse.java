package com.opsmemory.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ListMemoriesResponse {
    private List<MemoryInfo> items;
    private int total;
    private int limit;
    private int offset;

    public List<MemoryInfo> getItems() { return items; }
    public void setItems(List<MemoryInfo> items) { this.items = items; }
    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }
    public int getLimit() { return limit; }
    public void setLimit(int limit) { this.limit = limit; }
    public int getOffset() { return offset; }
    public void setOffset(int offset) { this.offset = offset; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MemoryInfo {
        private String id;
        private String text;
        private String date;
        
        @JsonProperty("fact_type")
        private String factType;
        
        @JsonProperty("document_id")
        private String documentId;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public String getFactType() { return factType; }
        public void setFactType(String factType) { this.factType = factType; }
        public String getDocumentId() { return documentId; }
        public void setDocumentId(String documentId) { this.documentId = documentId; }
    }
}
