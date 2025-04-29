package dev.aparikh.jsonplaceholder.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Configuration class for RestClient.
 */
@Configuration
public class RestClientConfig {

    private static final String JSON_PLACEHOLDER_BASE_URL = "https://jsonplaceholder.typicode.com";

    /**
     * Creates a RestClient bean configured to interact with the JSONPlaceholder API.
     *
     * @return A configured RestClient instance
     */
    @Bean
    public RestClient jsonPlaceholderRestClient() {
        return RestClient.builder()
                .baseUrl(JSON_PLACEHOLDER_BASE_URL)
                .build();
    }
}