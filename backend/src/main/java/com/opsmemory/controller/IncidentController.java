package com.opsmemory.controller;

import com.opsmemory.api.dto.IncidentAnalyzeRequest;
import com.opsmemory.api.dto.IncidentAnalyzeResponse;
import com.opsmemory.service.IncidentAgentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {
    private static final Logger log = LoggerFactory.getLogger(IncidentController.class);

    private final IncidentAgentService agentService;

    public IncidentController(IncidentAgentService agentService) {
        this.agentService = agentService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<IncidentAnalyzeResponse> analyzeIncident(@RequestBody IncidentAnalyzeRequest request) {
        if (request.getIncident() == null || request.getIncident().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        log.info("Received analyze request for incident: {}", request.getIncident());
        try {
            IncidentAnalyzeResponse response = agentService.analyzeIncident(request.getIncident());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error analyzing incident", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
