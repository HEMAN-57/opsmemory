package com.opsmemory.client.dto.groq;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GroqChatResponse {
    private List<Choice> choices;

    public List<Choice> getChoices() { return choices; }
    public void setChoices(List<Choice> choices) { this.choices = choices; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Choice {
        private GroqMessage message;

        public GroqMessage getMessage() { return message; }
        public void setMessage(GroqMessage message) { this.message = message; }
    }
}
