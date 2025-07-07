package dev.aparikh.jsonplaceholder.dto.claude;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * Response DTO for Claude API completions
 */
public record ClaudeResponse(
        @JsonProperty("id")
        String id,
        
        @JsonProperty("type")
        String type,
        
        @JsonProperty("role")
        String role,
        
        @JsonProperty("content")
        List<Content> content,
        
        @JsonProperty("model")
        String model,
        
        @JsonProperty("stop_reason")
        String stopReason,
        
        @JsonProperty("stop_sequence")
        String stopSequence,
        
        @JsonProperty("usage")
        Usage usage
) {
    
    public record Content(
            @JsonProperty("type")
            String type,
            
            @JsonProperty("text")
            String text,
            
            @JsonProperty("name")
            String name,
            
            @JsonProperty("input")
            Map<String, Object> input
    ) {}
    
    public record Usage(
            @JsonProperty("input_tokens")
            Integer inputTokens,
            
            @JsonProperty("output_tokens")
            Integer outputTokens,
            
            @JsonProperty("cache_creation_input_tokens")
            Integer cacheCreationInputTokens,
            
            @JsonProperty("cache_read_input_tokens")
            Integer cacheReadInputTokens
    ) {}
    
    /**
     * Helper method to get the first text content from the response
     * @return the text content or null if no text content found
     */
    public String getFirstTextContent() {
        if (content == null || content.isEmpty()) {
            return null;
        }
        
        return content.stream()
                .filter(c -> "text".equals(c.type()))
                .map(Content::text)
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Helper method to get all text content concatenated
     * @return concatenated text content
     */
    public String getAllTextContent() {
        if (content == null || content.isEmpty()) {
            return "";
        }
        
        return content.stream()
                .filter(c -> "text".equals(c.type()))
                .map(Content::text)
                .reduce("", (a, b) -> a + b);
    }
    
    /**
     * Helper method to check if the response contains tool use
     * @return true if response contains tool use
     */
    public boolean hasToolUse() {
        if (content == null || content.isEmpty()) {
            return false;
        }
        
        return content.stream()
                .anyMatch(c -> "tool_use".equals(c.type()));
    }
}
