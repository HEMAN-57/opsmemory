package com.opsmemory.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response from POST /v1/default/banks/{bank_id}/memories
 * Maps to the RetainResponse schema in the Hindsight OpenAPI spec.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RetainResponse {

    private boolean success;

    @JsonProperty("bank_id")
    private String bankId;

    @JsonProperty("items_count")
    private int itemsCount;

    @JsonProperty("async")
    private boolean async;

    @JsonProperty("operation_id")
    private String operationId;

    // Getters and setters

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getBankId() { return bankId; }
    public void setBankId(String bankId) { this.bankId = bankId; }

    public int getItemsCount() { return itemsCount; }
    public void setItemsCount(int itemsCount) { this.itemsCount = itemsCount; }

    public boolean isAsync() { return async; }
    public void setAsync(boolean async) { this.async = async; }

    public String getOperationId() { return operationId; }
    public void setOperationId(String operationId) { this.operationId = operationId; }

    @Override
    public String toString() {
        return "RetainResponse{success=" + success + ", bankId='" + bankId +
                "', itemsCount=" + itemsCount + ", async=" + async + "}";
    }
}
