package dev.aparikh.jsonplaceholder.controller;

import dev.aparikh.jsonplaceholder.model.ApiResponse;
import dev.aparikh.jsonplaceholder.model.Post;
import dev.aparikh.jsonplaceholder.service.DocumentService;
import dev.aparikh.jsonplaceholder.service.JsonPlaceholderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST controller for accessing JSONPlaceholder data.
 */
@RestController
@RequestMapping("/api/posts")
public class JsonPlaceholderController {

    private static final Logger logger = LoggerFactory.getLogger(JsonPlaceholderController.class);
    private final JsonPlaceholderService jsonPlaceholderService;
    private final DocumentService documentService;

    @Autowired
    public JsonPlaceholderController(JsonPlaceholderService jsonPlaceholderService, DocumentService documentService) {
        this.jsonPlaceholderService = jsonPlaceholderService;
        this.documentService = documentService;
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

    /**
     * Generic endpoint to fetch any type of data from the JSONPlaceholder API.
     * 
     * @param path The path to the resource (e.g., "/posts", "/users", "/comments")
     * @return A ResponseEntity containing an ApiResponse with the requested data as a Map
     */
    @GetMapping("/generic/{path}")
    public ResponseEntity<ApiResponse<Object>> getGenericData(@PathVariable String path) {
        logger.info("Received request to get generic data from path: {}", path);
        try {
            // Use the generic method to fetch data as a Map (or any other appropriate type)
            Object data = jsonPlaceholderService.getForObject("/" + path, Object.class);
            return ResponseEntity.ok(ApiResponse.success(data));
        } catch (Exception e) {
            logger.error("Error retrieving generic data from path: {}", path, e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve data: " + e.getMessage()));
        }
    }

    /**
     * Generic endpoint to fetch any type of data from the JSONPlaceholder API with an ID.
     * 
     * @param path The path to the resource (e.g., "posts", "users", "comments")
     * @param id The ID of the resource to fetch
     * @return A ResponseEntity containing an ApiResponse with the requested data as a Map
     */
    @GetMapping("/generic/{path}/{id}")
    public ResponseEntity<ApiResponse<Object>> getGenericDataById(
            @PathVariable String path, 
            @PathVariable String id) {
        logger.info("Received request to get generic data from path: {}/{}", path, id);
        try {
            // Create a map for URI variables
            Map<String, Object> uriVariables = new HashMap<>();
            uriVariables.put("id", id);

            // Use the generic method to fetch data as a Map (or any other appropriate type)
            Object data = jsonPlaceholderService.getForObject("/" + path + "/{id}", Object.class, uriVariables);
            return ResponseEntity.ok(ApiResponse.success(data));
        } catch (Exception e) {
            logger.error("Error retrieving generic data from path: {}/{}", path, id, e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve data: " + e.getMessage()));
        }
    }

    /**
     * Generic endpoint to fetch a list of any type of data from the JSONPlaceholder API with query parameters.
     * 
     * @param path The path to the resource (e.g., "posts", "users", "comments")
     * @param params A map of query parameters
     * @return A ResponseEntity containing an ApiResponse with the requested data as a List of Maps
     */
    @GetMapping("/generic/{path}/query")
    public ResponseEntity<ApiResponse<List<Object>>> getGenericDataWithParams(
            @PathVariable String path,
            @RequestParam Map<String, String> params) {
        logger.info("Received request to get generic data from path: {} with params: {}", path, params);
        try {
            // Build the query string
            StringBuilder queryString = new StringBuilder("/" + path + "?");
            for (Map.Entry<String, String> entry : params.entrySet()) {
                queryString.append(entry.getKey()).append("=").append("{").append(entry.getKey()).append("}").append("&");
            }
            // Remove the trailing &
            String uri = queryString.substring(0, queryString.length() - 1);

            // Use the generic method to fetch data as a List of Maps
            List<Object> data = jsonPlaceholderService.getForObject(
                    uri, 
                    new ParameterizedTypeReference<List<Object>>() {}, 
                    new HashMap<>(params));
            return ResponseEntity.ok(ApiResponse.success(data));
        } catch (Exception e) {
            logger.error("Error retrieving generic data from path: {} with params: {}", path, params, e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve data: " + e.getMessage()));
        }
    }

    /**
     * Generates a document (PDF, DOCX, or RTF) containing posts.
     *
     * @param format The format of the document to generate (pdf, docx, rtf)
     * @param userId Optional user ID to filter posts by user
     * @param postId Optional post ID to get a specific post
     * @return A ResponseEntity containing the generated document
     */
    @GetMapping("/document")
    public ResponseEntity<byte[]> generateDocument(
            @RequestParam(defaultValue = "pdf") String format,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long postId) {
        logger.info("Received request to generate {} document with userId: {}, postId: {}", format, userId, postId);

        try {
            // Fetch posts based on parameters
            List<Post> posts;
            if (postId != null) {
                // Get a specific post
                Optional<Post> post = jsonPlaceholderService.getPostById(postId);
                posts = post.map(List::of).orElse(List.of());
            } else if (userId != null) {
                // Get posts by user ID
                posts = jsonPlaceholderService.getPostsByUserId(userId);
            } else {
                // Get all posts
                posts = jsonPlaceholderService.getAllPosts();
            }

            if (posts.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body("No posts found".getBytes());
            }

            // Convert posts to HTML
            String html = documentService.renderPostsToHtml(posts);

            // Convert HTML to the requested format
            byte[] document = documentService.convertHtmlToFormat(html, format);

            // Set appropriate headers based on format
            HttpHeaders headers = new HttpHeaders();
            headers.setContentDispositionFormData("attachment", "posts." + format.toLowerCase());

            switch (format.toLowerCase()) {
                case "pdf":
                    headers.setContentType(MediaType.APPLICATION_PDF);
                    break;
                case "docx":
                    headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"));
                    break;
                case "rtf":
                    headers.setContentType(MediaType.parseMediaType("application/rtf"));
                    break;
                default:
                    return ResponseEntity
                            .status(HttpStatus.BAD_REQUEST)
                            .body(("Unsupported format: " + format).getBytes());
            }

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .body(document);
        } catch (Exception e) {
            logger.error("Error generating document", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Failed to generate document: " + e.getMessage()).getBytes());
        }
    }
}
