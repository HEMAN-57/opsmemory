package com.opsmemory.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/**
 * Response from POST /v1/default/banks/{bank_id}/memories/recall
 * Maps to the RecallResponse schema in the Hindsight OpenAPI spec.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RecallResponse {

    private List<RecallResult> results;
    private Map<String, Object> trace;
    private Map<String, Object> entities;

    public List<RecallResult> getResults() { return results; }
    public void setResults(List<RecallResult> results) { this.results = results; }

    public Map<String, Object> getTrace() { return trace; }
    public void setTrace(Map<String, Object> trace) { this.trace = trace; }

    public Map<String, Object> getEntities() { return entities; }
    public void setEntities(Map<String, Object> entities) { this.entities = entities; }

    @Override
    public String toString() {
        return "RecallResponse{resultCount=" + (results != null ? results.size() : 0) + "}";
    }

    /**
     * A single recalled memory result.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RecallResult {

        private String id;
        private String type;
        private String text;
        private String context;

        @JsonProperty("chunk_id")
        private String chunkId;

        @JsonProperty("occurred_start")
        private String occurredStart;

        @JsonProperty("occurred_end")
        private String occurredEnd;

        private List<String> entities;

        private List<String> tags;

        // Getters and setters

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }

        public String getContext() { return context; }
        public void setContext(String context) { this.context = context; }

        public String getChunkId() { return chunkId; }
        public void setChunkId(String chunkId) { this.chunkId = chunkId; }

        public String getOccurredStart() { return occurredStart; }
        public void setOccurredStart(String occurredStart) { this.occurredStart = occurredStart; }

        public String getOccurredEnd() { return occurredEnd; }
        public void setOccurredEnd(String occurredEnd) { this.occurredEnd = occurredEnd; }

        public List<String> getEntities() { return entities; }
        public void setEntities(List<String> entities) { this.entities = entities; }

        public List<String> getTags() { return tags; }
        public void setTags(List<String> tags) { this.tags = tags; }

        @Override
        public String toString() {
            return "RecallResult{id='" + id + "', type='" + type +
                    "', text='" + (text != null && text.length() > 80 ? text.substring(0, 80) + "..." : text) + "'}";
        }
    }
}
