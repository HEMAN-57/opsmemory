package com.opsmemory.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

@Configuration
public class HindsightConfig {

    private static final Logger log = LoggerFactory.getLogger(HindsightConfig.class);

    @Value("${hindsight.api-url}")
    private String hindsightApiUrl;

    @Value("${hindsight.api-key:}")
    private String hindsightApiKey;

    @Bean
    public WebClient hindsightWebClient(WebClient.Builder builder) {
        log.info("Configuring Hindsight WebClient → {}", hindsightApiUrl);
        builder = builder
                .baseUrl(hindsightApiUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        
        if (hindsightApiKey != null && !hindsightApiKey.isBlank()) {
            builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + hindsightApiKey);
            log.info("Hindsight WebClient configured with API Key authentication");
        } else {
            log.warn("Hindsight WebClient configured WITHOUT API Key authentication");
        }
        
        return builder.filter(logRequest()).build();
    }

    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(request -> {
            log.debug("Hindsight → {} {}", request.method(), request.url());
            return Mono.just(request);
        });
    }
}
