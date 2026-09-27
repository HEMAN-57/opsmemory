package com.opsmemory.client;

import com.opsmemory.client.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * REST client for the Hindsight Memory API.
 *
 * Verified endpoints (from the official Hindsight OpenAPI spec v0.10.1):
 *   Retain:  POST /v1/default/banks/{bank_id}/memories
 *   Recall:  POST /v1/default/banks/{bank_id}/memories/recall
 *   Health:  GET  /health
 */
@Service
public class HindsightClientService {

    private static final Logger log = LoggerFactory.getLogger(HindsightClientService.class);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(120);

    private final WebClient webClient;
    private final String bankId;

    public HindsightClientService(
            WebClient hindsightWebClient,
            @Value("${hindsight.bank-id}") String bankId) {
        this.webClient = hindsightWebClient;
        this.bankId = bankId;
        log.info("HindsightClientService initialised — bank: {}", bankId);
    }

    // ── Health ──────────────────────────────────────────────────────────

    /**
     * Check if Hindsight is reachable and healthy.
     */
    public boolean isHealthy() {
        try {
            webClient.get()
                    .uri("/health")
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, resp ->
                            Mono.error(new RuntimeException("Hindsight health check failed: " + resp.statusCode())))
                    .bodyToMono(Map.class)
                    .block(Duration.ofSeconds(10));
            return true;
        } catch (Exception e) {
            log.warn("Hindsight health check failed: {}", e.getMessage());
            return false;
        }
    }

    // ── Retain ──────────────────────────────────────────────────────────

    /**
     * Retain one or more memory items in Hindsight.
     * POST /v1/default/banks/{bank_id}/memories
     */
    public RetainResponse retain(List<MemoryItem> items) {
        RetainRequest request = new RetainRequest(items, false);
        log.info("Retaining {} items in bank '{}'", items.size(), bankId);

        try {
            RetainResponse response = webClient.post()
                    .uri("/v1/default/banks/{bankId}/memories", bankId)
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, resp ->
                            resp.bodyToMono(String.class)
                                    .flatMap(body -> Mono.error(new RuntimeException(
                                            "Hindsight retain failed [" + resp.statusCode() + "]: " + body))))
                    .bodyToMono(RetainResponse.class)
                    .block(REQUEST_TIMEOUT);

            log.info("Retain response: {}", response);
            return response;
        } catch (Exception e) {
            log.error("Failed to retain memories: {}", e.getMessage(), e);
            throw new RuntimeException("Hindsight retain failed", e);
        }
    }

    /**
     * Retain a single memory item.
     */
    public RetainResponse retain(MemoryItem item) {
        return retain(List.of(item));
    }

    // ── Recall ──────────────────────────────────────────────────────────

    /**
     * Recall memories relevant to a query.
     * POST /v1/default/banks/{bank_id}/memories/recall
     */
    public RecallResponse recall(String query) {
        return recall(new RecallRequest(query));
    }

    /**
     * Recall memories with full control over the request parameters.
     */
    public RecallResponse recall(RecallRequest request) {
        log.info("Recalling from bank '{}' — query: '{}'", bankId,
                request.getQuery().length() > 60
                        ? request.getQuery().substring(0, 60) + "..."
                        : request.getQuery());

        try {
            RecallResponse response = webClient.post()
                    .uri("/v1/default/banks/{bankId}/memories/recall", bankId)
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, resp ->
                            resp.bodyToMono(String.class)
                                    .flatMap(body -> Mono.error(new RuntimeException(
                                            "Hindsight recall failed [" + resp.statusCode() + "]: " + body))))
                    .bodyToMono(RecallResponse.class)
                    .block(REQUEST_TIMEOUT);

            int count = response != null && response.getResults() != null
                    ? response.getResults().size() : 0;
            log.info("Recall returned {} results", count);
            return response;
        } catch (Exception e) {
            log.error("Failed to recall memories: {}", e.getMessage(), e);
            throw new RuntimeException("Hindsight recall failed", e);
        }
    }

    // ── List Memories ───────────────────────────────────────────────────

    /**
     * List all memories in the bank.
     * GET /v1/default/banks/{bank_id}/memories/list
     */
    public ListMemoriesResponse listMemories(int limit, int offset) {
        log.info("Listing memories for bank '{}' (limit={}, offset={})", bankId, limit, offset);

        try {
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1/default/banks/{bankId}/memories/list")
                            .queryParam("limit", limit)
                            .queryParam("offset", offset)
                            .build(bankId))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, resp ->
                            resp.bodyToMono(String.class)
                                    .flatMap(body -> Mono.error(new RuntimeException(
                                            "Hindsight list memories failed [" + resp.statusCode() + "]: " + body))))
                    .bodyToMono(ListMemoriesResponse.class)
                    .block(REQUEST_TIMEOUT);
        } catch (Exception e) {
            log.error("Failed to list memories: {}", e.getMessage(), e);
            throw new RuntimeException("Hindsight list memories failed", e);
        }
    }

    /**
     * List memories in the bank (for duplicate checking).
     * GET /v1/default/banks/{bank_id}/memories/list?document_id=...
     */
    public boolean hasDocument(String documentId) {
        try {
            Map response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1/default/banks/{bankId}/memories/list")
                            .queryParam("document_id", documentId)
                            .queryParam("limit", 1)
                            .build(bankId))
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, resp -> {
                        // 404 means bank doesn't exist yet — not an error for our check
                        if (resp.statusCode().value() == 404) {
                            return Mono.empty();
                        }
                        return resp.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new RuntimeException(
                                        "List memories failed: " + body)));
                    })
                    .bodyToMono(Map.class)
                    .block(Duration.ofSeconds(15));

            if (response == null) return false;

            Object total = response.get("total");
            if (total instanceof Number) {
                return ((Number) total).intValue() > 0;
            }
            // Fallback: check if items list is non-empty
            Object items = response.get("items");
            if (items instanceof List) {
                return !((List<?>) items).isEmpty();
            }
            return false;
        } catch (Exception e) {
            log.debug("hasDocument check for '{}' returned error (bank may not exist yet): {}",
                    documentId, e.getMessage());
            return false;
        }
    }

    public String getBankId() {
        return bankId;
    }
}
