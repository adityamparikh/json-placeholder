package dev.aparikh.jsonplaceholder.service

import dev.aparikh.jsonplaceholder.model.Post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactor.mono
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

/**
 * Service class for interacting with the JSONPlaceholder API using Kotlin coroutines.
 */
@Service
class JsonPlaceholderService(
    private val jsonPlaceholderWebClient: WebClient
) {
    private val logger = LoggerFactory.getLogger(JsonPlaceholderService::class.java)

    /**
     * Retrieves all posts from the JSONPlaceholder API.
     *
     * @return A Mono containing a list of all posts
     */
    @Cacheable(value = ["posts"])
    fun getAllPosts(): Mono<List<Post>> = mono(Dispatchers.IO) {
        logger.info("Fetching all posts from JSONPlaceholder API")
        try {
            jsonPlaceholderWebClient.get()
                .uri("/posts")
                .retrieve()
                .bodyToMono(object : ParameterizedTypeReference<List<Post>>() {})
                .block() ?: emptyList()
        } catch (e: Exception) {
            logger.error("Error fetching all posts from JSONPlaceholder API", e)
            throw RuntimeException("Failed to fetch posts from external API", e)
        }
    }

    /**
     * Alias for getAllPosts for backward compatibility.
     */
    fun getAllPostsMono(): Mono<List<Post>> = getAllPosts()

    /**
     * Retrieves a specific post by its ID.
     *
     * @param id The ID of the post to retrieve
     * @return A Mono containing the post if found, or an empty post if not found
     */
    @Cacheable(value = ["posts"], key = "#id")
    fun getPostById(id: Long): Mono<Post> = mono(Dispatchers.IO) {
        logger.info("Fetching post with ID: {}", id)
        try {
            jsonPlaceholderWebClient.get()
                .uri("/posts/{id}", id)
                .retrieve()
                .bodyToMono(Post::class.java)
                .block() ?: Post()
        } catch (e: Exception) {
            logger.error("Error fetching post with ID: {}", id, e)
            Post()
        }
    }

    /**
     * Alias for getPostById for backward compatibility.
     */
    fun getPostByIdMono(id: Long): Mono<Post> = getPostById(id)

    /**
     * Retrieves posts by user ID.
     *
     * @param userId The ID of the user whose posts to retrieve
     * @return A Mono containing a list of posts by the specified user
     */
    @Cacheable(value = ["postsByUser"], key = "#userId")
    fun getPostsByUserId(userId: Long): Mono<List<Post>> = mono(Dispatchers.IO) {
        logger.info("Fetching posts for user with ID: {}", userId)
        try {
            jsonPlaceholderWebClient.get()
                .uri("/posts?userId={userId}", userId)
                .retrieve()
                .bodyToMono(object : ParameterizedTypeReference<List<Post>>() {})
                .block() ?: emptyList()
        } catch (e: Exception) {
            logger.error("Error fetching posts for user with ID: {}", userId, e)
            throw RuntimeException("Failed to fetch posts for user from external API", e)
        }
    }

    /**
     * Alias for getPostsByUserId for backward compatibility.
     */
    fun getPostsByUserIdMono(userId: Long): Mono<List<Post>> = getPostsByUserId(userId)

    /**
     * Generic method to fetch data from any endpoint with any return type.
     *
     * @param endpoint The API endpoint to call
     * @param responseType The class of the expected response type
     * @param T The type parameter for the response
     * @return A Mono containing the response body converted to the specified type
     */
    @Cacheable(value = ["apiData"], key = "{ #endpoint, #responseType }")
    fun <T : Any> getForObject(endpoint: String, responseType: Class<T>): Mono<T> = mono(Dispatchers.IO) {
        logger.info("Fetching data from endpoint: {}", endpoint)
        jsonPlaceholderWebClient.get()
            .uri(endpoint)
            .retrieve()
            .bodyToMono(responseType)
            .block()!!
    }

    /**
     * Generic method to fetch data from any endpoint with any parameterized return type.
     *
     * @param endpoint The API endpoint to call
     * @param responseType The ParameterizedTypeReference for the expected response type
     * @param T The type parameter for the response
     * @return A Mono containing the response body converted to the specified type
     */
    @Cacheable(value = ["apiData"], key = "#endpoint")
    fun <T : Any> getForObject(endpoint: String, responseType: ParameterizedTypeReference<T>): Mono<T> = mono(Dispatchers.IO) {
        logger.info("Fetching data from endpoint: {}", endpoint)
        jsonPlaceholderWebClient.get()
            .uri(endpoint)
            .retrieve()
            .bodyToMono(responseType)
            .block()!!
    }

    /**
     * Generic method to fetch data from any endpoint with any return type and path variables.
     *
     * @param endpoint The API endpoint to call
     * @param responseType The class of the expected response type
     * @param uriVariables The variables to expand in the URI template
     * @param T The type parameter for the response
     * @return A Mono containing the response body converted to the specified type
     */
    @Cacheable(value = ["apiData"], key = "{ #endpoint, #responseType, #uriVariables }")
    fun <T : Any> getForObject(endpoint: String, responseType: Class<T>, uriVariables: Map<String, Any>): Mono<T> = mono(Dispatchers.IO) {
        logger.info("Fetching data from endpoint: {} with variables: {}", endpoint, uriVariables)
        jsonPlaceholderWebClient.get()
            .uri(endpoint, uriVariables)
            .retrieve()
            .bodyToMono(responseType)
            .block()!!
    }

    /**
     * Generic method to fetch data from any endpoint with any parameterized return type and path variables.
     *
     * @param endpoint The API endpoint to call
     * @param responseType The ParameterizedTypeReference for the expected response type
     * @param uriVariables The variables to expand in the URI template
     * @param T The type parameter for the response
     * @return A Mono containing the response body converted to the specified type
     */
    @Cacheable(value = ["apiData"], key = "{ #endpoint, #uriVariables }")
    fun <T : Any> getForObject(endpoint: String, responseType: ParameterizedTypeReference<T>, uriVariables: Map<String, Any>): Mono<T> = mono(Dispatchers.IO) {
        logger.info("Fetching data from endpoint: {} with variables: {}", endpoint, uriVariables)
        jsonPlaceholderWebClient.get()
            .uri(endpoint, uriVariables)
            .retrieve()
            .bodyToMono(responseType)
            .block()!!
    }
}
