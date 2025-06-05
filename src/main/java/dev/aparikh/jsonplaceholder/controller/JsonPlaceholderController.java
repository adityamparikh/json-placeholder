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
import reactor.core.publisher.Mono;

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
    public Mono<ResponseEntity<ApiResponse<List<Post>>>> getAllPosts() {
        logger.info("Received request to get all posts");
        return jsonPlaceholderService.getAllPosts()
                .map(posts -> ResponseEntity.ok(ApiResponse.success(posts)))
                .onErrorReturn(ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Failed to retrieve posts")));
    }

    /**
     * Retrieves a specific post by its ID.
     *
     * @param id The ID of the post to retrieve
     * @return A ResponseEntity containing an ApiResponse with the requested post
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<Post>>> getPostById(@PathVariable Long id) {
        logger.info("Received request to get post with ID: {}", id);
        return jsonPlaceholderService.getPostById(id)
                .filter(post -> post.getId() != null)
                .map(post -> ResponseEntity.ok(ApiResponse.success(post)))
                .switchIfEmpty(Mono.just(ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Post not found with ID: " + id))))
                .onErrorReturn(ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Failed to retrieve post")));
    }

    /**
     * Retrieves posts by user ID.
     *
     * @param userId The ID of the user whose posts to retrieve
     * @return A ResponseEntity containing an ApiResponse with a list of posts by the specified user
     */
    @GetMapping("/user/{userId}")
    public Mono<ResponseEntity<ApiResponse<List<Post>>>> getPostsByUserId(@PathVariable Long userId) {
        logger.info("Received request to get posts for user with ID: {}", userId);
        return jsonPlaceholderService.getPostsByUserId(userId)
                .map(posts -> ResponseEntity.ok(ApiResponse.success(posts)))
                .onErrorReturn(ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Failed to retrieve posts for user")));
    }

    /**
     * Generic endpoint to fetch any type of data from the JSONPlaceholder API.
     * 
     * @param path The path to the resource (e.g., "/posts", "/users", "/comments")
     * @return A ResponseEntity containing an ApiResponse with the requested data as a Map
     */
    @GetMapping("/generic/{path}")
    public Mono<ResponseEntity<ApiResponse<Object>>> getGenericData(@PathVariable String path) {
        logger.info("Received request to get generic data from path: {}", path);
        return jsonPlaceholderService.getForObject("/" + path, Object.class)
                .map(data -> ResponseEntity.ok(ApiResponse.success(data)))
                .onErrorReturn(ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Failed to retrieve data")));
    }

    /**
     * Generic endpoint to fetch any type of data from the JSONPlaceholder API with an ID.
     * 
     * @param path The path to the resource (e.g., "posts", "users", "comments")
     * @param id The ID of the resource to fetch
     * @return A ResponseEntity containing an ApiResponse with the requested data as a Map
     */
    @GetMapping("/generic/{path}/{id}")
    public Mono<ResponseEntity<ApiResponse<Object>>> getGenericDataById(
            @PathVariable String path, 
            @PathVariable String id) {
        logger.info("Received request to get generic data from path: {}/{}", path, id);
        // Create a map for URI variables
        Map<String, Object> uriVariables = new HashMap<>();
        uriVariables.put("id", id);

        return jsonPlaceholderService.getForObject("/" + path + "/{id}", Object.class, uriVariables)
                .map(data -> ResponseEntity.ok(ApiResponse.success(data)))
                .onErrorReturn(ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Failed to retrieve data")));
    }

    /**
     * Generic endpoint to fetch a list of any type of data from the JSONPlaceholder API with query parameters.
     * 
     * @param path The path to the resource (e.g., "posts", "users", "comments")
     * @param params A map of query parameters
     * @return A ResponseEntity containing an ApiResponse with the requested data as a List of Maps
     */
    @GetMapping("/generic/{path}/query")
    public Mono<ResponseEntity<ApiResponse<List<Object>>>> getGenericDataWithParams(
            @PathVariable String path,
            @RequestParam Map<String, String> params) {
        logger.info("Received request to get generic data from path: {} with params: {}", path, params);
        // Build the query string
        StringBuilder queryString = new StringBuilder("/" + path + "?");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            queryString.append(entry.getKey()).append("=").append("{").append(entry.getKey()).append("}").append("&");
        }
        // Remove the trailing &
        String uri = queryString.substring(0, queryString.length() - 1);

        return jsonPlaceholderService.getForObject(
                uri,
                new ParameterizedTypeReference<List<Object>>() {},
                new HashMap<>(params))
                .map(data -> ResponseEntity.ok(ApiResponse.success(data)))
                .onErrorReturn(ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Failed to retrieve data")));
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
    public Mono<ResponseEntity<byte[]>> generateDocument(
            @RequestParam(defaultValue = "pdf") String format,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long postId) {
        logger.info("Received request to generate {} document with userId: {}, postId: {}", format, userId, postId);

        // Fetch posts based on parameters
        Mono<List<Post>> postsMono;
        if (postId != null) {
            // Get a specific post
            postsMono = jsonPlaceholderService.getPostById(postId)
                    .filter(post -> post.getId() != null)
                    .map(List::of)
                    .switchIfEmpty(Mono.just(List.of()));
        } else if (userId != null) {
            // Get posts by user ID
            postsMono = jsonPlaceholderService.getPostsByUserId(userId);
        } else {
             // Get all posts
            postsMono = jsonPlaceholderService.getAllPosts();
        }

        return postsMono
                .flatMap(posts -> {
                    if (posts.isEmpty()) {
                        return Mono.just(ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body("No posts found".getBytes()));
                    }

                    return documentService.renderPostsToHtml(posts)
                            .flatMap(html -> documentService.convertHtmlToFormat(html, format))
                            .map(document -> {
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
                            });
                })
                .onErrorReturn(ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to generate document".getBytes()));
    }
}
