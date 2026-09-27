package com.opsmemory.client.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for DTO serialisation/deserialisation.
 * Verifies that our DTOs match the Hindsight OpenAPI contract.
 */
class DtoSerializationTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void retainRequest_serializes_correctly() throws Exception {
        MemoryItem item = new MemoryItem(
                "Auth service had a cert expiry",
                "incident review",
                "inc-001"
        );
        RetainRequest request = new RetainRequest(List.of(item), false);

        String json = mapper.writeValueAsString(request);

        assertTrue(json.contains("\"items\""));
        assertTrue(json.contains("\"content\""));
        assertTrue(json.contains("\"context\""));
        assertTrue(json.contains("\"document_id\""));
        assertTrue(json.contains("\"async\":false"));
        assertFalse(json.contains("\"timestamp\""), "null timestamp should be excluded");
    }

    @Test
    void retainResponse_deserializes_correctly() throws Exception {
        String json = """
                {
                  "success": true,
                  "bank_id": "opsmemory",
                  "items_count": 3,
                  "async": false,
                  "usage": {"input_tokens": 500, "output_tokens": 100, "total_tokens": 600}
                }
                """;

        RetainResponse response = mapper.readValue(json, RetainResponse.class);

        assertTrue(response.isSuccess());
        assertEquals("opsmemory", response.getBankId());
        assertEquals(3, response.getItemsCount());
        assertFalse(response.isAsync());
    }

    @Test
    void recallRequest_serializes_correctly() throws Exception {
        RecallRequest request = new RecallRequest("What caused the payment outage?");

        String json = mapper.writeValueAsString(request);

        assertTrue(json.contains("\"query\""));
        assertTrue(json.contains("\"budget\":\"mid\""));
        assertTrue(json.contains("\"max_tokens\":4096"));
        assertTrue(json.contains("\"types\""));
        assertTrue(json.contains("\"world\""));
        assertTrue(json.contains("\"experience\""));
        assertTrue(json.contains("\"observation\""));
    }

    @Test
    void recallResponse_deserializes_correctly() throws Exception {
        String json = """
                {
                  "results": [
                    {
                      "id": "abc-123",
                      "type": "world",
                      "text": "Payment service uses HikariCP connection pool",
                      "context": "incident review",
                      "entities": ["payment-service", "HikariCP"]
                    },
                    {
                      "id": "def-456",
                      "type": "experience",
                      "text": "We resolved the connection pool issue by rolling back",
                      "occurred_start": "2026-08-10T14:23:00Z"
                    }
                  ],
                  "trace": {"num_results": 2, "time_seconds": 0.05}
                }
                """;

        RecallResponse response = mapper.readValue(json, RecallResponse.class);

        assertNotNull(response.getResults());
        assertEquals(2, response.getResults().size());

        RecallResponse.RecallResult first = response.getResults().get(0);
        assertEquals("abc-123", first.getId());
        assertEquals("world", first.getType());
        assertEquals("Payment service uses HikariCP connection pool", first.getText());
        assertEquals(List.of("payment-service", "HikariCP"), first.getEntities());

        RecallResponse.RecallResult second = response.getResults().get(1);
        assertEquals("experience", second.getType());
        assertEquals("2026-08-10T14:23:00Z", second.getOccurredStart());

        assertNotNull(response.getTrace());
    }

    @Test
    void memoryItem_with_timestamp_serializes() throws Exception {
        MemoryItem item = new MemoryItem(
                "Server crashed",
                "outage",
                "inc-099",
                "2026-09-01T10:00:00Z"
        );

        String json = mapper.writeValueAsString(item);

        assertTrue(json.contains("\"timestamp\":\"2026-09-01T10:00:00Z\""));
        assertTrue(json.contains("\"document_id\":\"inc-099\""));
    }
}
