package com.example.ratelimitedapi.controller;

import com.example.ratelimitedapi.annotation.RateLimit;
import com.example.ratelimitedapi.service.RateLimitingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class DummyApiController {

    @Autowired
    private RateLimitingService rateLimitingService;

    @Autowired
    private HttpServletRequest request;

    @GetMapping("/dummy")
    @RateLimit(key = "dummy-get")
    public ResponseEntity<Map<String, Object>> getDummyData() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Hello from dummy API!");
        response.put("timestamp", LocalDateTime.now());
        response.put("method", "GET");
        response.put("endpoint", "/api/dummy");
        response.put("clientIp", getClientIpAddress());
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/dummy")
    @RateLimit(key = "dummy-post")
    public ResponseEntity<Map<String, Object>> postDummyData(@RequestBody(required = false) Map<String, Object> requestBody) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Data received successfully!");
        response.put("timestamp", LocalDateTime.now());
        response.put("method", "POST");
        response.put("endpoint", "/api/dummy");
        response.put("receivedData", requestBody);
        response.put("clientIp", getClientIpAddress());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/dummy/{id}")
    @RateLimit(key = "dummy-get-by-id")
    public ResponseEntity<Map<String, Object>> getDummyDataById(@PathVariable String id) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Dummy data for ID: " + id);
        response.put("id", id);
        response.put("timestamp", LocalDateTime.now());
        response.put("method", "GET");
        response.put("endpoint", "/api/dummy/" + id);
        response.put("clientIp", getClientIpAddress());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getRateLimitStatus() {
        String clientIp = getClientIpAddress();
        Map<String, Object> response = new HashMap<>();
        response.put("clientIp", clientIp);
        response.put("availableTokens", Map.of(
            "dummy-get", rateLimitingService.getAvailableTokens("dummy-get:" + clientIp),
            "dummy-post", rateLimitingService.getAvailableTokens("dummy-post:" + clientIp),
            "dummy-get-by-id", rateLimitingService.getAvailableTokens("dummy-get-by-id:" + clientIp)
        ));
        response.put("rateLimitInfo", "10 requests per minute per endpoint");
        response.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }

    private String getClientIpAddress() {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}