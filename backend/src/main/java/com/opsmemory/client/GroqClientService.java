package com.opsmemory.client;

import com.opsmemory.client.dto.groq.GroqChatRequest;
import com.opsmemory.client.dto.groq.GroqChatResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class GroqClientService {
    private static final Logger log = LoggerFactory.getLogger(GroqClientService.class);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(60);

    private final WebClient groqWebClient;

    public GroqClientService(WebClient groqWebClient) {
        this.groqWebClient = groqWebClient;
    }

    public GroqChatResponse chatCompletion(GroqChatRequest request) {
        log.info("Sending chat completion request to Groq (model: {})", request.getModel());
        
        try {
            return groqWebClient.post()
                    .uri("/chat/completions")
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, resp ->
                            resp.bodyToMono(String.class)
                                    .flatMap(body -> Mono.error(new RuntimeException(
                                            "Groq API error [" + resp.statusCode() + "]: " + body))))
                    .bodyToMono(GroqChatResponse.class)
                    .block(REQUEST_TIMEOUT);
        } catch (Exception e) {
            log.error("Failed to call Groq API: {}", e.getMessage(), e);
            throw new RuntimeException("Groq API call failed", e);
        }
    }
}
