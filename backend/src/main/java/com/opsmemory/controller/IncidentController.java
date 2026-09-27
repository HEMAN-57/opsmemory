package com.opsmemory.controller;

import com.opsmemory.api.dto.IncidentAnalyzeRequest;
import com.opsmemory.api.dto.IncidentAnalyzeResponse;
import com.opsmemory.api.dto.IncidentResolveRequest;
import com.opsmemory.api.dto.IncidentResolveResponse;
import com.opsmemory.client.dto.ListMemoriesResponse;
import com.opsmemory.service.IncidentAgentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

@RestController
@RequestMapping("/api/incidents")
@CrossOrigin(origins = "*")
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
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @PostMapping("/resolve")
    public ResponseEntity<IncidentResolveResponse> resolveIncident(@RequestBody IncidentResolveRequest request) {
        if (request.getIncident() == null || request.getIncident().isBlank() ||
            request.getResolution() == null || request.getResolution().isBlank() ||
            request.getOutcome() == null || request.getOutcome().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        log.info("Received resolve request for incident");
        try {
            IncidentResolveResponse response = agentService.resolveIncident(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error resolving incident", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/history")
    public ResponseEntity<ListMemoriesResponse> getIncidentHistory(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        log.info("Received history request");
        try {
            ListMemoriesResponse response = agentService.getIncidentHistory(limit, offset);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting incident history", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
