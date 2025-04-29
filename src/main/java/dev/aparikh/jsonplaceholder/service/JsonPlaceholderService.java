package dev.aparikh.jsonplaceholder.service;

import dev.aparikh.jsonplaceholder.model.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Optional;

/**
 * Service class for interacting with the JSONPlaceholder API.
 */
@Service
public class JsonPlaceholderService {

    private static final Logger logger = LoggerFactory.getLogger(JsonPlaceholderService.class);
    private final RestClient restClient;

    @Autowired
    public JsonPlaceholderService(RestClient jsonPlaceholderRestClient) {
        this.restClient = jsonPlaceholderRestClient;
    }

    /**
     * Retrieves all posts from the JSONPlaceholder API.
     *
     * @return A list of all posts
     */
    public List<Post> getAllPosts() {
        logger.info("Fetching all posts from JSONPlaceholder API");
        return restClient.get()
                .uri("/posts")
                .retrieve()
                .body(new ParameterizedTypeReference<List<Post>>() {});
    }

    /**
     * Retrieves a specific post by its ID.
     *
     * @param id The ID of the post to retrieve
     * @return An Optional containing the post if found, or empty if not found
     */
    public Optional<Post> getPostById(Long id) {
        logger.info("Fetching post with ID: {}", id);
        try {
            Post post = restClient.get()
                    .uri("/posts/{id}", id)
                    .retrieve()
                    .body(Post.class);
            return Optional.ofNullable(post);
        } catch (Exception e) {
            logger.error("Error fetching post with ID: {}", id, e);
            return Optional.empty();
        }
    }

    /**
     * Retrieves posts by user ID.
     *
     * @param userId The ID of the user whose posts to retrieve
     * @return A list of posts by the specified user
     */
    public List<Post> getPostsByUserId(Long userId) {
        logger.info("Fetching posts for user with ID: {}", userId);
        return restClient.get()
                .uri("/posts?userId={userId}", userId)
                .retrieve()
                .body(new ParameterizedTypeReference<List<Post>>() {});
    }
}