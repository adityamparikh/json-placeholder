package dev.aparikh.jsonplaceholder.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

/**
 * Configuration class for WebClient.
 */
@Configuration
class WebClientConfig {

    companion object {
        private const val JSON_PLACEHOLDER_BASE_URL = "https://jsonplaceholder.typicode.com"
    }

    /**
     * Creates a WebClient bean configured to interact with the JSONPlaceholder API.
     *
     * @return A configured WebClient instance
     */
    @Bean
    fun jsonPlaceholderWebClient(): WebClient {
        return WebClient.builder()
            .baseUrl(JSON_PLACEHOLDER_BASE_URL)
            .build()
    }
}