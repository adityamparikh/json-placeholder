package dev.aparikh.jsonplaceholder.controller;

import dev.aparikh.jsonplaceholder.model.ApiResponse;
import dev.aparikh.jsonplaceholder.model.Post;
import dev.aparikh.jsonplaceholder.service.JsonPlaceholderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for accessing JSONPlaceholder data.
 */
@RestController
@RequestMapping("/api/posts")
public class JsonPlaceholderController {

    private static final Logger logger = LoggerFactory.getLogger(JsonPlaceholderController.class);
    private final JsonPlaceholderService jsonPlaceholderService;

    @Autowired
    public JsonPlaceholderController(JsonPlaceholderService jsonPlaceholderService) {
        this.jsonPlaceholderService = jsonPlaceholderService;
    }

    /**
     * Retrieves all posts.
     *
     * @return A ResponseEntity containing an ApiResponse with a list of all posts
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Post>>> getAllPosts() {
        logger.info("Received request to get all posts");
        try {
            List<Post> posts = jsonPlaceholderService.getAllPosts();
            return ResponseEntity.ok(ApiResponse.success(posts));
        } catch (Exception e) {
            logger.error("Error retrieving all posts", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve posts: " + e.getMessage()));
        }
    }

    /**
     * Retrieves a specific post by its ID.
     *
     * @param id The ID of the post to retrieve
     * @return A ResponseEntity containing an ApiResponse with the requested post
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Post>> getPostById(@PathVariable Long id) {
        logger.info("Received request to get post with ID: {}", id);
        try {
            Optional<Post> post = jsonPlaceholderService.getPostById(id);
            return post
                    .map(p -> ResponseEntity.ok(ApiResponse.success(p)))
                    .orElseGet(() -> ResponseEntity
                            .status(HttpStatus.NOT_FOUND)
                            .body(ApiResponse.error("Post not found with ID: " + id)));
        } catch (Exception e) {
            logger.error("Error retrieving post with ID: {}", id, e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve post: " + e.getMessage()));
        }
    }

    /**
     * Retrieves posts by user ID.
     *
     * @param userId The ID of the user whose posts to retrieve
     * @return A ResponseEntity containing an ApiResponse with a list of posts by the specified user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<Post>>> getPostsByUserId(@PathVariable Long userId) {
        logger.info("Received request to get posts for user with ID: {}", userId);
        try {
            List<Post> posts = jsonPlaceholderService.getPostsByUserId(userId);
            return ResponseEntity.ok(ApiResponse.success(posts));
        } catch (Exception e) {
            logger.error("Error retrieving posts for user with ID: {}", userId, e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve posts for user: " + e.getMessage()));
        }
    }
}