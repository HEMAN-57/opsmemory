package com.opsmemory.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opsmemory.api.dto.AnalysisDetails;
import com.opsmemory.api.dto.IncidentAnalyzeResponse;
import com.opsmemory.client.GroqClientService;
import com.opsmemory.client.HindsightClientService;
import com.opsmemory.client.dto.RecallResponse;
import com.opsmemory.client.dto.groq.GroqChatRequest;
import com.opsmemory.client.dto.groq.GroqChatResponse;
import com.opsmemory.client.dto.groq.GroqMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class IncidentAgentService {
    private static final Logger log = LoggerFactory.getLogger(IncidentAgentService.class);

    private final HindsightClientService hindsightClient;
    private final GroqClientService groqClient;
    private final ObjectMapper objectMapper;
    private final String model;

    public IncidentAgentService(
            HindsightClientService hindsightClient,
            GroqClientService groqClient,
            ObjectMapper objectMapper,
            @Value("${groq.model:llama-3.3-70b-versatile}") String model) {
        this.hindsightClient = hindsightClient;
        this.groqClient = groqClient;
        this.objectMapper = objectMapper;
        this.model = model;
    }

    public IncidentAnalyzeResponse analyzeIncident(String incidentText) {
        log.info("Analyzing incident: {}", incidentText);

        // 1. Recall historical memory from Hindsight
        RecallResponse recallResponse = hindsightClient.recall(incidentText);
        List<String> memories = new ArrayList<>();
        boolean memoryFound = false;

        if (recallResponse != null && recallResponse.getResults() != null && !recallResponse.getResults().isEmpty()) {
            memoryFound = true;
            memories = recallResponse.getResults().stream()
                    .map(RecallResponse.RecallResult::getText)
                    .collect(Collectors.toList());
        }

        // 2. Prepare the prompt
        String systemPrompt = "You are an expert Incident Response Commander.\n"
                + "Your job is to analyze the current production incident and provide actionable recommendations.\n"
                + "Always clearly distinguish historical facts (from memory) from your current inference.\n"
                + "Do not invent or hallucinate historical incidents.\n"
                + "You must respond strictly in JSON format matching this schema:\n"
                + "{\n"
                + "  \"severity\": \"(e.g. SEV1, SEV2, SEV3)\",\n"
                + "  \"likelyRootCause\": \"...\",\n"
                + "  \"immediateActions\": [\"action1\", \"action2\"],\n"
                + "  \"reasoning\": \"...\",\n"
                + "  \"historicalReference\": \"...\"\n"
                + "}";

        String userPrompt = "Current Incident:\n" + incidentText + "\n\n";
        
        if (memoryFound) {
            userPrompt += "Historical Memories Recalled from Hindsight:\n";
            for (int i = 0; i < memories.size(); i++) {
                userPrompt += (i + 1) + ". " + memories.get(i) + "\n";
            }
        } else {
            userPrompt += "Historical Memories: No relevant historical memory found.\n";
            userPrompt += "Please state in 'historicalReference' that no relevant historical memory was found.\n";
        }

        // 3. Call Groq LLM
        List<GroqMessage> messages = List.of(
                new GroqMessage("system", systemPrompt),
                new GroqMessage("user", userPrompt)
        );

        GroqChatRequest request = new GroqChatRequest(model, messages, 0.2);
        request.setResponseFormat(Map.of("type", "json_object"));

        GroqChatResponse groqResponse = groqClient.chatCompletion(request);
        String jsonContent = groqResponse.getChoices().get(0).getMessage().getContent();

        // 4. Parse response
        AnalysisDetails analysis;
        try {
            analysis = objectMapper.readValue(jsonContent, AnalysisDetails.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse Groq response: {}", jsonContent, e);
            throw new RuntimeException("Failed to parse LLM analysis", e);
        }

        // 5. Construct final response
        IncidentAnalyzeResponse response = new IncidentAnalyzeResponse();
        response.setIncident(incidentText);
        response.setMemoryFound(memoryFound);
        response.setRecalledMemories(memories);
        response.setAnalysis(analysis);

        return response;
    }

    public com.opsmemory.api.dto.IncidentResolveResponse resolveIncident(com.opsmemory.api.dto.IncidentResolveRequest request) {
        log.info("Resolving incident");

        String documentId = "resolve-" + java.util.UUID.randomUUID().toString();
        
        StringBuilder content = new StringBuilder();
        content.append("INCIDENT:\n").append(request.getIncident()).append("\n\n");
        if (request.getRootCause() != null && !request.getRootCause().isBlank()) {
            content.append("ROOT CAUSE:\n").append(request.getRootCause()).append("\n\n");
        }
        content.append("RESOLUTION:\n").append(request.getResolution()).append("\n\n");
        content.append("OUTCOME:\n").append(request.getOutcome());

        com.opsmemory.client.dto.MemoryItem item = new com.opsmemory.client.dto.MemoryItem(
                content.toString(),
                "Post-incident resolution review",
                documentId
        );

        com.opsmemory.client.dto.RetainResponse retainResponse = hindsightClient.retain(item);

        return new com.opsmemory.api.dto.IncidentResolveResponse(
                retainResponse.isSuccess(),
                documentId,
                retainResponse.isSuccess() ? "Incident successfully retained in Hindsight memory." : "Failed to retain incident memory."
        );
    }

    public com.opsmemory.client.dto.ListMemoriesResponse getIncidentHistory(int limit, int offset) {
        return hindsightClient.listMemories(limit, offset);
    }
}
