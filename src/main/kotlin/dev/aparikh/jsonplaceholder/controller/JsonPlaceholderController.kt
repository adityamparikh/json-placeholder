package dev.aparikh.jsonplaceholder.controller

import dev.aparikh.jsonplaceholder.model.ApiResponse
import dev.aparikh.jsonplaceholder.model.Post
import dev.aparikh.jsonplaceholder.service.DocumentService
import dev.aparikh.jsonplaceholder.service.JsonPlaceholderService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactor.mono
import org.slf4j.LoggerFactory
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.util.*

/**
 * REST controller for accessing JSONPlaceholder data.
 */
@RestController
@RequestMapping("/api/posts")
class JsonPlaceholderController(
    private val jsonPlaceholderService: JsonPlaceholderService,
    private val documentService: DocumentService
) {
    private val logger = LoggerFactory.getLogger(JsonPlaceholderController::class.java)

    /**
     * Retrieves all posts.
     *
     * @return A ResponseEntity containing an ApiResponse with a list of all posts
     */
    @GetMapping
    fun getAllPosts(): Mono<ResponseEntity<ApiResponse<List<Post>>>> {
        logger.info("Received request to get all posts")
        val postsMono = jsonPlaceholderService.getAllPostsMono() ?: Mono.just(emptyList())
        return postsMono
            .map { posts -> ResponseEntity.ok(ApiResponse.success(posts)) }
            .onErrorResume { e ->
                logger.error("Error fetching all posts", e)
                Mono.just(
                    ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Failed to retrieve posts"))
                )
            }
    }

    /**
     * Retrieves a specific post by its ID.
     *
     * @param id The ID of the post to retrieve
     * @return A ResponseEntity containing an ApiResponse with the requested post
     */
    @GetMapping("/{id}")
    fun getPostById(@PathVariable id: Long): Mono<ResponseEntity<ApiResponse<Post>>> {
        logger.info("Received request to get post with ID: {}", id)
        return jsonPlaceholderService.getPostById(id)
            .map { post ->
                if (post.id != null) {
                    ResponseEntity.ok(ApiResponse.success(post))
                } else {
                    ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Post not found with ID: $id"))
                }
            }
            .onErrorResume { e ->
                logger.error("Error fetching post with ID: {}", id, e)
                Mono.just(
                    ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Failed to retrieve post"))
                )
            }
    }

    /**
     * Retrieves posts by user ID.
     *
     * @param userId The ID of the user whose posts to retrieve
     * @return A ResponseEntity containing an ApiResponse with a list of posts by the specified user
     */
    @GetMapping("/user/{userId}")
    fun getPostsByUserId(@PathVariable userId: Long): Mono<ResponseEntity<ApiResponse<List<Post>>>> {
        logger.info("Received request to get posts for user with ID: {}", userId)
        return jsonPlaceholderService.getPostsByUserId(userId)
            .map { posts -> ResponseEntity.ok(ApiResponse.success(posts)) }
            .onErrorResume { e ->
                logger.error("Error fetching posts for user with ID: {}", userId, e)
                Mono.just(
                    ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Failed to retrieve posts for user"))
                )
            }
    }

    /**
     * Generic endpoint to fetch any type of data from the JSONPlaceholder API.
     * 
     * @param path The path to the resource (e.g., "/posts", "/users", "/comments")
     * @return A ResponseEntity containing an ApiResponse with the requested data as a Map
     */
    @GetMapping("/generic/{path}")
    fun getGenericData(@PathVariable path: String): Mono<ResponseEntity<ApiResponse<Any>>> {
        logger.info("Received request to get generic data from path: {}", path)
        return jsonPlaceholderService.getForObject("/$path", Any::class.java)
            .map { data -> ResponseEntity.ok(ApiResponse.success(data)) }
            .onErrorResume { e ->
                logger.error("Error fetching data from path: {}", path, e)
                Mono.just(
                    ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Failed to retrieve data"))
                )
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
    fun getGenericDataById(
        @PathVariable path: String, 
        @PathVariable id: String
    ): Mono<ResponseEntity<ApiResponse<Any>>> {
        logger.info("Received request to get generic data from path: {}/{}", path, id)
        // Create a map for URI variables
        val uriVariables = mapOf("id" to id)
        return jsonPlaceholderService.getForObject("/$path/{id}", Any::class.java, uriVariables)
            .map { data -> ResponseEntity.ok(ApiResponse.success(data)) }
            .onErrorResume { e ->
                logger.error("Error fetching data from path: {}/{}", path, id, e)
                Mono.just(
                    ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error("Failed to retrieve data"))
                )
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
    fun getGenericDataWithParams(
        @PathVariable path: String,
        @RequestParam params: Map<String, String>
    ): Mono<ResponseEntity<ApiResponse<List<Any>>>> {
        logger.info("Received request to get generic data from path: {} with params: {}", path, params)

        // Build the query string
        val queryString = StringBuilder("/$path?")
        for ((key, _) in params) {
            queryString.append(key).append("=").append("{").append(key).append("}").append("&")
        }
        // Remove the trailing &
        val uri = queryString.substring(0, queryString.length - 1)

        return jsonPlaceholderService.getForObject(
            uri,
            object : ParameterizedTypeReference<List<Any>>() {},
            HashMap(params)
        )
        .map { data -> ResponseEntity.ok(ApiResponse.success(data)) }
        .onErrorResume { e ->
            logger.error("Error fetching data from path: {} with params: {}", path, params, e)
            Mono.just(
                ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to retrieve data"))
            )
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
    fun generateDocument(
        @RequestParam(defaultValue = "pdf") format: String,
        @RequestParam(required = false) userId: Long?,
        @RequestParam(required = false) postId: Long?
    ): Mono<ResponseEntity<ByteArray>> {
        logger.info("Received request to generate {} document with userId: {}, postId: {}", format, userId, postId)

        // Fetch posts based on parameters
        val postsMono: Mono<List<Post>> = when {
            postId != null -> {
                // Get a specific post
                jsonPlaceholderService.getPostById(postId)
                    .map { post -> 
                        if (post.id != null) listOf(post) else emptyList() 
                    }
            }
            userId != null -> {
                // Get posts by user ID
                jsonPlaceholderService.getPostsByUserId(userId)
            }
            else -> {
                // Get all posts
                jsonPlaceholderService.getAllPosts()
            }
        }

        return postsMono.flatMap { posts ->
            if (posts.isEmpty()) {
                return@flatMap Mono.just(
                    ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body("No posts found".toByteArray())
                )
            }

            documentService.renderPostsToHtml(posts)
                .flatMap { html -> documentService.convertHtmlToFormat(html, format) }
                .map { document ->
                    // Set appropriate headers based on format
                    val headers = HttpHeaders()
                    headers.setContentDispositionFormData("attachment", "posts.${format.toLowerCase(Locale.getDefault())}")

                    when (format.toLowerCase(Locale.getDefault())) {
                        "pdf" -> headers.contentType = MediaType.APPLICATION_PDF
                        "docx" -> headers.contentType = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                        "rtf" -> headers.contentType = MediaType.parseMediaType("application/rtf")
                        else -> return@map ResponseEntity
                            .status(HttpStatus.BAD_REQUEST)
                            .body("Unsupported format: $format".toByteArray())
                    }

                    ResponseEntity.ok().headers(headers).body(document)
                }
        }.onErrorResume { e ->
            logger.error("Error generating document", e)
            Mono.just(
                ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to generate document".toByteArray())
            )
        }
    }
}
