package com.opsmemory.client.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Request body for POST /v1/default/banks/{bank_id}/memories
 * Maps to the RetainRequest schema in the Hindsight OpenAPI spec.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RetainRequest {

    private List<MemoryItem> items;

    @JsonProperty("async")
    private Boolean async;

    public RetainRequest() {}

    public RetainRequest(List<MemoryItem> items, boolean async) {
        this.items = items;
        this.async = async;
    }

    public List<MemoryItem> getItems() { return items; }
    public void setItems(List<MemoryItem> items) { this.items = items; }

    public Boolean getAsync() { return async; }
    public void setAsync(Boolean async) { this.async = async; }
}
