package com.opsmemory.client.dto.groq;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class GroqChatRequest {
    private String model;
    private List<GroqMessage> messages;
    private Double temperature;

    @JsonProperty("response_format")
    private Map<String, String> responseFormat;

    public GroqChatRequest() {}

    public GroqChatRequest(String model, List<GroqMessage> messages, Double temperature) {
        this.model = model;
        this.messages = messages;
        this.temperature = temperature;
    }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public List<GroqMessage> getMessages() { return messages; }
    public void setMessages(List<GroqMessage> messages) { this.messages = messages; }
    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }
    public Map<String, String> getResponseFormat() { return responseFormat; }
    public void setResponseFormat(Map<String, String> responseFormat) { this.responseFormat = responseFormat; }
}
