package dev.aparikh.jsonplaceholder.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuration for Claude API integration
 */
@Configuration
public class ClaudeConfig {
    
    @Bean
    @ConfigurationProperties(prefix = "claude.api")
    public ClaudeProperties claudeProperties() {
        return new ClaudeProperties();
    }
    
    @Bean
    public WebClient claudeWebClient(ClaudeProperties claudeProperties) {
        return WebClient.builder()
                .baseUrl(claudeProperties.getBaseUrl())
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("anthropic-version", claudeProperties.getApiVersion())
                .defaultHeader("x-api-key", claudeProperties.getApiKey())
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(10 * 1024 * 1024)) // 10MB buffer for large responses
                .build();
    }
    
    /**
     * Configuration properties for Claude API
     */
    public static class ClaudeProperties {
        private String apiKey;
        private String baseUrl = "https://api.anthropic.com";
        private String apiVersion = "2023-06-01";
        private String defaultModel = "claude-3-5-sonnet-20241022";
        private Integer defaultMaxTokens = 1000;
        private Double defaultTemperature = 0.7;
        private Long timeoutSeconds = 120L;
        
        // Getters and setters
        public String getApiKey() {
            return apiKey;
        }
        
        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }
        
        public String getBaseUrl() {
            return baseUrl;
        }
        
        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }
        
        public String getApiVersion() {
            return apiVersion;
        }
        
        public void setApiVersion(String apiVersion) {
            this.apiVersion = apiVersion;
        }
        
        public String getDefaultModel() {
            return defaultModel;
        }
        
        public void setDefaultModel(String defaultModel) {
            this.defaultModel = defaultModel;
        }
        
        public Integer getDefaultMaxTokens() {
            return defaultMaxTokens;
        }
        
        public void setDefaultMaxTokens(Integer defaultMaxTokens) {
            this.defaultMaxTokens = defaultMaxTokens;
        }
        
        public Double getDefaultTemperature() {
            return defaultTemperature;
        }
        
        public void setDefaultTemperature(Double defaultTemperature) {
            this.defaultTemperature = defaultTemperature;
        }
        
        public Long getTimeoutSeconds() {
            return timeoutSeconds;
        }
        
        public void setTimeoutSeconds(Long timeoutSeconds) {
            this.timeoutSeconds = timeoutSeconds;
        }
    }
}
