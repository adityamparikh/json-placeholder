package dev.aparikh.jsonplaceholder.service;

import dev.aparikh.jsonplaceholder.config.ClaudeConfig;
import dev.aparikh.jsonplaceholder.dto.claude.ClaudeRequest;
import dev.aparikh.jsonplaceholder.dto.claude.ClaudeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;

/**
 * Service for interacting with Claude API
 */
@Service
public class ClaudeService {
    
    private static final Logger logger = LoggerFactory.getLogger(ClaudeService.class);
    
    private final WebClient claudeWebClient;
    private final ClaudeConfig.ClaudeProperties claudeProperties;
    
    public ClaudeService(@Qualifier("claudeWebClient") WebClient claudeWebClient,
                         ClaudeConfig.ClaudeProperties claudeProperties) {
        this.claudeWebClient = claudeWebClient;
        this.claudeProperties = claudeProperties;
    }
    
    /**
     * Send a simple text completion request to Claude
     */
    public Mono<String> complete(String prompt) {
        return complete(prompt, null);
    }
    
    /**
     * Send a text completion request to Claude with system prompt
     */
    public Mono<String> complete(String prompt, String systemPrompt) {
        ClaudeRequest request = ClaudeRequest.builder()
                .model(claudeProperties.getDefaultModel())
                .maxTokens(claudeProperties.getDefaultMaxTokens())
                .temperature(claudeProperties.getDefaultTemperature())
                .messages(List.of(new ClaudeRequest.Message("user", prompt)))
                .system(systemPrompt)
                .build();
        
        return sendRequest(request)
                .map(ClaudeResponse::getFirstTextContent);
    }
    
    /**
     * Send a cached completion request (useful for expensive operations)
     */
    @Cacheable(value = "claude-completions", key = "#prompt.hashCode()")
    public Mono<String> completeCached(String prompt) {
        logger.info("Cache miss for prompt hash: {}", prompt.hashCode());
        return complete(prompt);
    }
    
    /**
     * Send a structured request to Claude
     */
    public Mono<ClaudeResponse> sendRequest(ClaudeRequest request) {
        logger.debug("Sending request to Claude API: {}", request.model());
        
        return claudeWebClient
                .post()
                .uri("/v1/messages")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ClaudeResponse.class)
                .timeout(Duration.ofSeconds(claudeProperties.getTimeoutSeconds()))
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                        .filter(this::isRetryableException))
                .doOnSuccess(response -> logger.debug("Received response from Claude API"))
                .doOnError(error -> logger.error("Error calling Claude API", error));
    }
    
    /**
     * Send a conversation request with multiple messages
     */
    public Mono<String> conversation(List<ClaudeRequest.Message> messages, String systemPrompt) {
        ClaudeRequest request = ClaudeRequest.builder()
                .model(claudeProperties.getDefaultModel())
                .maxTokens(claudeProperties.getDefaultMaxTokens())
                .temperature(claudeProperties.getDefaultTemperature())
                .messages(messages)
                .system(systemPrompt)
                .build();
        
        return sendRequest(request)
                .map(ClaudeResponse::getFirstTextContent);
    }
    
    /**
     * Analyze text with Claude (useful for content analysis)
     */
    public Mono<String> analyzeText(String text, String analysisType) {
        String systemPrompt = switch (analysisType.toLowerCase()) {
            case "sentiment" -> "You are a sentiment analysis expert. Analyze the sentiment of the given text and provide a brief summary.";
            case "summary" -> "You are a text summarization expert. Provide a concise summary of the given text.";
            case "keywords" -> "You are a keyword extraction expert. Extract the main keywords and topics from the given text.";
            case "language" -> "You are a language detection expert. Identify the language of the given text and provide confidence level.";
            default -> "You are a text analysis expert. Analyze the given text and provide insights.";
        };
        
        return complete(text, systemPrompt);
    }
    
    /**
     * Generate creative content with Claude
     */
    public Mono<String> generateContent(String prompt, String contentType, Double creativity) {
        String systemPrompt = switch (contentType.toLowerCase()) {
            case "story" -> "You are a creative storyteller. Write engaging and imaginative stories.";
            case "poem" -> "You are a poet. Create beautiful and meaningful poetry.";
            case "essay" -> "You are an essay writer. Create well-structured and informative essays.";
            case "code" -> "You are a software developer. Write clean, efficient, and well-documented code.";
            default -> "You are a creative writing assistant. Generate high-quality content.";
        };
        
        ClaudeRequest request = ClaudeRequest.builder()
                .model(claudeProperties.getDefaultModel())
                .maxTokens(2000) // More tokens for creative content
                .temperature(creativity != null ? creativity : 0.9) // Higher creativity
                .messages(List.of(new ClaudeRequest.Message("user", prompt)))
                .system(systemPrompt)
                .build();
        
        return sendRequest(request)
                .map(ClaudeResponse::getFirstTextContent);
    }
    
    /**
     * Check if an exception is retryable
     */
    private boolean isRetryableException(Throwable throwable) {
        if (throwable instanceof WebClientResponseException wcre) {
            int statusCode = wcre.getStatusCode().value();
            // Retry on server errors and rate limiting
            return statusCode >= 500 || statusCode == 429;
        }
        return false;
    }
    
    /**
     * Health check for Claude API
     */
    public Mono<Boolean> healthCheck() {
        return complete("Hello")
                .map(response -> response != null && !response.trim().isEmpty())
                .doOnSuccess(healthy -> logger.info("Claude API health check: {}", healthy ? "HEALTHY" : "UNHEALTHY"))
                .onErrorReturn(false);
    }
}
