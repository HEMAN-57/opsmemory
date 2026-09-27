package com.opsmemory.controller;

import com.opsmemory.client.HindsightClientService;
import com.opsmemory.client.dto.RecallRequest;
import com.opsmemory.client.dto.RecallResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Temporary development endpoints for verifying Phase 1 Hindsight integration.
 * These endpoints can be removed or secured once the full agent flow is implemented.
 */
@RestController
@RequestMapping("/api/dev")
public class DevVerificationController {

    private static final Logger log = LoggerFactory.getLogger(DevVerificationController.class);

    private final HindsightClientService hindsight;

    public DevVerificationController(HindsightClientService hindsight) {
        this.hindsight = hindsight;
    }

    /**
     * GET /api/dev/health
     * Check if Hindsight is reachable.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        boolean healthy = hindsight.isHealthy();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("hindsight_healthy", healthy);
        result.put("bank_id", hindsight.getBankId());
        return healthy
                ? ResponseEntity.ok(result)
                : ResponseEntity.status(503).body(result);
    }

    /**
     * GET /api/dev/memory-test
     * Sends a test recall query to Hindsight and returns the results.
     * Verifies that seeded incidents can be recalled.
     */
    @GetMapping("/memory-test")
    public ResponseEntity<Map<String, Object>> memoryTest(
            @RequestParam(value = "query", required = false) String customQuery) {

        String query = (customQuery != null && !customQuery.isBlank())
                ? customQuery
                : "Have we previously experienced database connection pool exhaustion or payment-service failures, and what resolution worked?";

        log.info("Memory test — query: '{}'", query);

        RecallRequest request = new RecallRequest(query);
        request.setTrace(true);

        RecallResponse response = hindsight.recall(request);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("query", query);
        result.put("bank_id", hindsight.getBankId());

        if (response != null && response.getResults() != null) {
            result.put("result_count", response.getResults().size());
            result.put("results", response.getResults().stream()
                    .map(r -> {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("id", r.getId());
                        m.put("type", r.getType());
                        m.put("text", r.getText());
                        if (r.getEntities() != null) m.put("entities", r.getEntities());
                        return m;
                    })
                    .collect(Collectors.toList()));
            if (response.getTrace() != null) {
                result.put("trace", response.getTrace());
            }
        } else {
            result.put("result_count", 0);
            result.put("results", List.of());
        }

        return ResponseEntity.ok(result);
    }

    /**
     * POST /api/dev/recall
     * Custom recall with full body control.
     */
    @PostMapping("/recall")
    public ResponseEntity<RecallResponse> recall(@RequestBody RecallRequest request) {
        RecallResponse response = hindsight.recall(request);
        return ResponseEntity.ok(response);
    }
}
