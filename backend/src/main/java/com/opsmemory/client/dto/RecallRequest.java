package com.opsmemory.client.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Request body for POST /v1/default/banks/{bank_id}/memories/recall
 * Maps to the RecallRequest schema in the Hindsight OpenAPI spec.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RecallRequest {

    private String query;

    private List<String> types;

    private String budget;

    @JsonProperty("max_tokens")
    private Integer maxTokens;

    private Boolean trace;

    private List<String> tags;

    @JsonProperty("tags_match")
    private String tagsMatch;

    public RecallRequest() {}

    public RecallRequest(String query) {
        this.query = query;
        this.budget = "mid";
        this.maxTokens = 4096;
        this.types = List.of("world", "experience", "observation");
    }

    // Getters and setters

    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }

    public List<String> getTypes() { return types; }
    public void setTypes(List<String> types) { this.types = types; }

    public String getBudget() { return budget; }
    public void setBudget(String budget) { this.budget = budget; }

    public Integer getMaxTokens() { return maxTokens; }
    public void setMaxTokens(Integer maxTokens) { this.maxTokens = maxTokens; }

    public Boolean getTrace() { return trace; }
    public void setTrace(Boolean trace) { this.trace = trace; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public String getTagsMatch() { return tagsMatch; }
    public void setTagsMatch(String tagsMatch) { this.tagsMatch = tagsMatch; }
}
