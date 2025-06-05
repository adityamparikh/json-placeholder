package dev.aparikh.jsonplaceholder.service;

import dev.aparikh.jsonplaceholder.model.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service class for interacting with the JSONPlaceholder API.
 */
@Service
public class JsonPlaceholderService {

    private static final Logger logger = LoggerFactory.getLogger(JsonPlaceholderService.class);
    private final WebClient webClient;

    @Autowired
    public JsonPlaceholderService(WebClient jsonPlaceholderWebClient) {
        this.webClient = jsonPlaceholderWebClient;
    }

    /**
     * Retrieves all posts from the JSONPlaceholder API.
     *
     * @return A list of all posts
     */
    @Cacheable(value = "posts")
    public Mono<List<Post>> getAllPosts() {
        logger.info("Fetching all posts from JSONPlaceholder API");
        return webClient.get()
                .uri("/posts")
                .retrieve()
                .bodyToFlux(Post.class)
                .collectList()
                .doOnError(e -> logger.error("Error fetching all posts from JSONPlaceholder API", e))
                .onErrorMap(e -> new RuntimeException("Failed to fetch posts from external API", e));
    }

    /**
     * Retrieves a specific post by its ID.
     *
     * @param id The ID of the post to retrieve
     * @return An Optional containing the post if found, or empty if not found
     */
    @Cacheable(value = "posts", key = "#id")
    public Mono<Post> getPostById(Long id) {
        logger.info("Fetching post with ID: {}", id);
        return webClient.get()
                .uri("/posts/{id}", id)
                .retrieve()
                .bodyToMono(Post.class)
                .doOnError(e -> logger.error("Error fetching post with ID: {}", id, e))
                .onErrorReturn(new Post());
    }

    /**
     * Retrieves posts by user ID.
     *
     * @param userId The ID of the user whose posts to retrieve
     * @return A list of posts by the specified user
     */
    @Cacheable(value = "postsByUser", key = "#userId")
    public Mono<List<Post>> getPostsByUserId(Long userId) {
        logger.info("Fetching posts for user with ID: {}", userId);
        return webClient.get()
                .uri("/posts?userId={userId}", userId)
                .retrieve()
                .bodyToFlux(Post.class)
                .collectList()
                .doOnError(e -> logger.error("Error fetching posts for user with ID: {}", userId, e))
                .onErrorMap(e -> new RuntimeException("Failed to fetch posts for user from external API", e));
    }

    /**
     * Generic method to fetch data from any endpoint with any return type.
     *
     * @param endpoint The API endpoint to call
     * @param responseType The class of the expected response type
     * @param <T> The type parameter for the response
     * @return The response body converted to the specified type
     */
    @Cacheable(value = "apiData", key = "{ #endpoint, #responseType }")
    public <T> Mono<T> getForObject(String endpoint, Class<T> responseType) {
        logger.info("Fetching data from endpoint: {}", endpoint);
        return webClient.get()
                .uri(endpoint)
                .retrieve()
                .bodyToMono(responseType);
    }

    /**
     * Generic method to fetch data from any endpoint with any parameterized return type.
     *
     * @param endpoint The API endpoint to call
     * @param responseType The ParameterizedTypeReference for the expected response type
     * @param <T> The type parameter for the response
     * @return The response body converted to the specified type
     */
    @Cacheable(value = "apiData", key = "#endpoint")
    public <T> Mono<T> getForObject(String endpoint, ParameterizedTypeReference<T> responseType) {
        logger.info("Fetching data from endpoint: {}", endpoint);
        return webClient.get()
                .uri(endpoint)
                .retrieve()
                .bodyToMono(responseType);
    }

    /**
     * Generic method to fetch data from any endpoint with any return type and path variables.
     *
     * @param endpoint The API endpoint to call
     * @param responseType The class of the expected response type
     * @param uriVariables The variables to expand in the URI template
     * @param <T> The type parameter for the response
     * @return The response body converted to the specified type
     */
    @Cacheable(value = "apiData", key = "{ #endpoint, #responseType, #uriVariables }")
    public <T> Mono<T> getForObject(String endpoint, Class<T> responseType, Map<String, Object> uriVariables) {
        logger.info("Fetching data from endpoint: {} with variables: {}", endpoint, uriVariables);
        return webClient.get()
                .uri(endpoint, uriVariables)
                .retrieve()
                .bodyToMono(responseType);
    }

    /**
     * Generic method to fetch data from any endpoint with any parameterized return type and path variables.
     *
     * @param endpoint The API endpoint to call
     * @param responseType The ParameterizedTypeReference for the expected response type
     * @param uriVariables The variables to expand in the URI template
     * @param <T> The type parameter for the response
     * @return The response body converted to the specified type
     */
    @Cacheable(value = "apiData", key = "{ #endpoint, #uriVariables }")
    public <T> Mono<T> getForObject(String endpoint, ParameterizedTypeReference<T> responseType, Map<String, Object> uriVariables) {
        logger.info("Fetching data from endpoint: {} with variables: {}", endpoint, uriVariables);
        return webClient.get()
                .uri(endpoint, uriVariables)
                .retrieve()
                .bodyToMono(responseType);
    }
}
