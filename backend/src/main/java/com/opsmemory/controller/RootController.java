package com.opsmemory.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class RootController {

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> root() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("service", "OpsMemory");
        result.put("status", "UP");
        result.put("message", "AI Incident Command Center API");
        return ResponseEntity.ok(result);
    }
}
