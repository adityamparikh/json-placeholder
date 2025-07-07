package dev.aparikh.jsonplaceholder.controller;

import dev.aparikh.jsonplaceholder.dto.claude.ClaudeRequest;
import dev.aparikh.jsonplaceholder.dto.claude.ClaudeResponse;
import dev.aparikh.jsonplaceholder.service.ClaudeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for Claude AI integration
 */
@RestController
@RequestMapping("/api/claude")
@CrossOrigin(origins = "*")
public class ClaudeController {
    
    private final ClaudeService claudeService;
    
    public ClaudeController(ClaudeService claudeService) {
        this.claudeService = claudeService;
    }
    
    /**
     * Simple text completion endpoint
     */
    @PostMapping("/complete")
    public Mono<ResponseEntity<Map<String, String>>> complete(@RequestBody Map<String, String> request) {
        String prompt = request.get("prompt");
        String systemPrompt = request.get("system");
        
        if (prompt == null || prompt.trim().isEmpty()) {
            return Mono.just(ResponseEntity.badRequest()
                    .body(Map.of("error", "Prompt is required")));
        }
        
        return claudeService.complete(prompt, systemPrompt)
                .map(response -> ResponseEntity.ok(Map.of(
                        "response", response,
                        "prompt", prompt
                )))
                .onErrorReturn(ResponseEntity.status(500)
                        .body(Map.of("error", "Failed to process request")));
    }
    
    /**
     * Advanced completion with full request structure
     */
    @PostMapping("/chat")
    public Mono<ResponseEntity<ClaudeResponse>> chat(@Valid @RequestBody ClaudeRequest request) {
        return claudeService.sendRequest(request)
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.status(500).build());
    }
    
    /**
     * Text analysis endpoint
     */
    @PostMapping("/analyze")
    public Mono<ResponseEntity<Map<String, String>>> analyzeText(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        String analysisType = request.getOrDefault("type", "general");
        
        if (text == null || text.trim().isEmpty()) {
            return Mono.just(ResponseEntity.badRequest()
                    .body(Map.of("error", "Text is required")));
        }
        
        return claudeService.analyzeText(text, analysisType)
                .map(analysis -> ResponseEntity.ok(Map.of(
                        "analysis", analysis,
                        "type", analysisType,
                        "originalText", text
                )))
                .onErrorReturn(ResponseEntity.status(500)
                        .body(Map.of("error", "Failed to analyze text")));
    }
    
    /**
     * Content generation endpoint
     */
    @PostMapping("/generate")
    public Mono<ResponseEntity<Map<String, Object>>> generateContent(@RequestBody Map<String, Object> request) {
        String prompt = (String) request.get("prompt");
        String contentType = (String) request.getOrDefault("type", "general");
        Double creativity = request.get("creativity") != null ? 
                Double.parseDouble(request.get("creativity").toString()) : null;
        
        if (prompt == null || prompt.trim().isEmpty()) {
            return Mono.just(ResponseEntity.badRequest()
                    .body(Map.of("error", "Prompt is required")));
        }
        
        return claudeService.generateContent(prompt, contentType, creativity)
                .map(content -> ResponseEntity.ok(Map.of(
                        "content", content,
                        "type", contentType,
                        "prompt", prompt,
                        "creativity", creativity != null ? creativity : 0.9
                )))
                .onErrorReturn(ResponseEntity.status(500)
                        .body(Map.of("error", "Failed to generate content")));
    }
    
    /**
     * Conversation endpoint with message history
     */
    @PostMapping("/conversation")
    public Mono<ResponseEntity<Map<String, String>>> conversation(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<Map<String, String>> messagesMap = (List<Map<String, String>>) request.get("messages");
        String systemPrompt = (String) request.get("system");
        
        if (messagesMap == null || messagesMap.isEmpty()) {
            return Mono.just(ResponseEntity.badRequest()
                    .body(Map.of("error", "Messages are required")));
        }
        
        List<ClaudeRequest.Message> messages = messagesMap.stream()
                .map(m -> new ClaudeRequest.Message(m.get("role"), m.get("content")))
                .toList();
        
        return claudeService.conversation(messages, systemPrompt)
                .map(response -> ResponseEntity.ok(Map.of(
                        "response", response,
                        "messageCount", String.valueOf(messages.size())
                )))
                .onErrorReturn(ResponseEntity.status(500)
                        .body(Map.of("error", "Failed to process conversation")));
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public Mono<ResponseEntity<Map<String, Object>>> healthCheck() {
        return claudeService.healthCheck()
                .map(healthy -> ResponseEntity.ok(Map.of(
                        "status", healthy ? "healthy" : "unhealthy",
                        "service", "claude-api",
                        "timestamp", System.currentTimeMillis()
                )))
                .onErrorReturn(ResponseEntity.status(500)
                        .body(Map.of("status", "error", "service", "claude-api")));
    }
    
    /**
     * Get available analysis types
     */
    @GetMapping("/analysis-types")
    public ResponseEntity<Map<String, Object>> getAnalysisTypes() {
        return ResponseEntity.ok(Map.of(
                "types", List.of("sentiment", "summary", "keywords", "language", "general"),
                "description", Map.of(
                        "sentiment", "Analyze the emotional tone of text",
                        "summary", "Create a concise summary of text",
                        "keywords", "Extract main keywords and topics",
                        "language", "Detect the language of text",
                        "general", "General text analysis"
                )
        ));
    }
    
    /**
     * Get available content generation types
     */
    @GetMapping("/content-types")
    public ResponseEntity<Map<String, Object>> getContentTypes() {
        return ResponseEntity.ok(Map.of(
                "types", List.of("story", "poem", "essay", "code", "general"),
                "description", Map.of(
                        "story", "Generate creative stories",
                        "poem", "Create poetry",
                        "essay", "Write structured essays",
                        "code", "Generate code snippets",
                        "general", "General content generation"
                ),
                "creativity", Map.of(
                        "range", "0.0 to 1.0",
                        "default", 0.9,
                        "description", "Higher values = more creative output"
                )
        ));
    }
}
