package dev.aparikh.jsonplaceholder.dto.claude;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

/**
 * Request DTO for Claude API completions
 */
public record ClaudeRequest(
        @JsonProperty("model")
        @NotBlank
        String model,
        
        @JsonProperty("max_tokens")
        @NotNull
        Integer maxTokens,
        
        @JsonProperty("messages")
        @NotNull
        List<Message> messages,
        
        @JsonProperty("temperature")
        Double temperature,
        
        @JsonProperty("system")
        String system,
        
        @JsonProperty("tools")
        List<Tool> tools,
        
        @JsonProperty("tool_choice")
        Object toolChoice
) {
    
    public record Message(
            @JsonProperty("role")
            @NotBlank
            String role,
            
            @JsonProperty("content")
            @NotBlank
            String content
    ) {}
    
    public record Tool(
            @JsonProperty("name")
            String name,
            
            @JsonProperty("description")
            String description,
            
            @JsonProperty("input_schema")
            Map<String, Object> inputSchema
    ) {}
    
    // Builder pattern for easy construction
    public static class Builder {
        private String model = "claude-3-5-sonnet-20241022";
        private Integer maxTokens = 1000;
        private List<Message> messages;
        private Double temperature;
        private String system;
        private List<Tool> tools;
        private Object toolChoice;
        
        public Builder model(String model) {
            this.model = model;
            return this;
        }
        
        public Builder maxTokens(Integer maxTokens) {
            this.maxTokens = maxTokens;
            return this;
        }
        
        public Builder messages(List<Message> messages) {
            this.messages = messages;
            return this;
        }
        
        public Builder temperature(Double temperature) {
            this.temperature = temperature;
            return this;
        }
        
        public Builder system(String system) {
            this.system = system;
            return this;
        }
        
        public Builder tools(List<Tool> tools) {
            this.tools = tools;
            return this;
        }
        
        public Builder toolChoice(Object toolChoice) {
            this.toolChoice = toolChoice;
            return this;
        }
        
        public ClaudeRequest build() {
            return new ClaudeRequest(model, maxTokens, messages, temperature, system, tools, toolChoice);
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
}
