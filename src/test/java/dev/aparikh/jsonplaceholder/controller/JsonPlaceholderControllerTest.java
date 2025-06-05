package dev.aparikh.jsonplaceholder.controller;

import dev.aparikh.jsonplaceholder.model.Post;
import dev.aparikh.jsonplaceholder.service.DocumentService;
import dev.aparikh.jsonplaceholder.service.JsonPlaceholderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.*;

import static org.mockito.Mockito.*;

public class JsonPlaceholderControllerTest {

    private WebTestClient webTestClient;

    @Mock
    private JsonPlaceholderService jsonPlaceholderService;

    @Mock
    private DocumentService documentService;

    @InjectMocks
    private JsonPlaceholderController controller;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        webTestClient = WebTestClient.bindToController(controller).build();
    }

    @Test
    public void getAllPosts_ShouldReturnAllPosts() {
        // Arrange
        List<Post> posts = Arrays.asList(
                new Post(1L, 1L, "Test Post 1", "This is test post 1"),
                new Post(2L, 1L, "Test Post 2", "This is test post 2")
        );
        when(jsonPlaceholderService.getAllPosts()).thenReturn(Mono.just(posts));

        // Act & Assert
        webTestClient.get()
                .uri("/api/posts")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("success")
                .jsonPath("$.data.length()").isEqualTo(2)
                .jsonPath("$.data[0].id").isEqualTo(1)
                .jsonPath("$.data[0].title").isEqualTo("Test Post 1")
                .jsonPath("$.data[1].id").isEqualTo(2)
                .jsonPath("$.data[1].title").isEqualTo("Test Post 2");

        verify(jsonPlaceholderService, times(1)).getAllPosts();
    }

    @Test
    public void getPostById_WhenPostExists_ShouldReturnPost() {
        // Arrange
        Post post = new Post(1L, 1L, "Test Post", "This is a test post");
        when(jsonPlaceholderService.getPostById(1L)).thenReturn(Mono.just(post));

        // Act & Assert
        webTestClient.get()
                .uri("/api/posts/1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("success")
                .jsonPath("$.data.id").isEqualTo(1)
                .jsonPath("$.data.title").isEqualTo("Test Post")
                .jsonPath("$.data.body").isEqualTo("This is a test post");

        verify(jsonPlaceholderService, times(1)).getPostById(1L);
    }

    @Test
    public void getPostById_WhenPostDoesNotExist_ShouldReturnNotFound() {
        // Arrange
        when(jsonPlaceholderService.getPostById(999L)).thenReturn(Mono.just(new Post()));

        // Act & Assert
        webTestClient.get()
                .uri("/api/posts/999")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo("error")
                .jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Post not found"));

        verify(jsonPlaceholderService, times(1)).getPostById(999L);
    }

    @Test
    public void getPostsByUserId_ShouldReturnUserPosts() {
        // Arrange
        List<Post> userPosts = Arrays.asList(
                new Post(1L, 1L, "User Post 1", "This is user post 1"),
                new Post(2L, 1L, "User Post 2", "This is user post 2")
        );
        when(jsonPlaceholderService.getPostsByUserId(1L)).thenReturn(Mono.just(userPosts));

        // Act & Assert
        webTestClient.get()
                .uri("/api/posts/user/1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("success")
                .jsonPath("$.data.length()").isEqualTo(2)
                .jsonPath("$.data[0].userId").isEqualTo(1)
                .jsonPath("$.data[0].title").isEqualTo("User Post 1")
                .jsonPath("$.data[1].userId").isEqualTo(1)
                .jsonPath("$.data[1].title").isEqualTo("User Post 2");

        verify(jsonPlaceholderService, times(1)).getPostsByUserId(1L);
    }

    @Test
    public void getGenericData_ShouldReturnGenericData() {
        // Arrange
        Map<String, Object> userData = new HashMap<>();
        userData.put("id", 1);
        userData.put("name", "John Doe");
        userData.put("email", "john.doe@example.com");

        when(jsonPlaceholderService.getForObject(eq("/users"), eq(Object.class))).thenReturn(Mono.just(userData));

        // Act & Assert
        webTestClient.get()
                .uri("/api/posts/generic/users")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("success")
                .jsonPath("$.data.id").isEqualTo(1)
                .jsonPath("$.data.name").isEqualTo("John Doe")
                .jsonPath("$.data.email").isEqualTo("john.doe@example.com");

        verify(jsonPlaceholderService, times(1)).getForObject(eq("/users"), eq(Object.class));
    }

    @Test
    public void generateDocument_WithPdfFormat_ShouldReturnPdfDocument() {
        // Arrange
        List<Post> posts = Arrays.asList(
                new Post(1L, 1L, "Test Post 1", "This is test post 1"),
                new Post(2L, 1L, "Test Post 2", "This is test post 2")
        );
        String html = "<html><body><h1>Posts</h1></body></html>";
        byte[] pdfBytes = "PDF content".getBytes();

        when(jsonPlaceholderService.getAllPosts()).thenReturn(Mono.just(posts));
        when(documentService.renderPostsToHtml(posts)).thenReturn(Mono.just(html));
        when(documentService.convertHtmlToFormat(html, "pdf")).thenReturn(Mono.just(pdfBytes));

        // Act & Assert
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/posts/document")
                        .queryParam("format", "pdf")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_PDF)
                .expectBody(byte[].class).isEqualTo(pdfBytes);

        verify(jsonPlaceholderService, times(1)).getAllPosts();
        verify(documentService, times(1)).renderPostsToHtml(posts);
        verify(documentService, times(1)).convertHtmlToFormat(html, "pdf");
    }

    @Test
    public void generateDocument_WithDocxFormatAndUserId_ShouldReturnDocxDocument() {
        // Arrange
        List<Post> userPosts = Arrays.asList(
                new Post(1L, 1L, "User Post 1", "This is user post 1"),
                new Post(2L, 1L, "User Post 2", "This is user post 2")
        );
        String html = "<html><body><h1>User Posts</h1></body></html>";
        byte[] docxBytes = "DOCX content".getBytes();

        when(jsonPlaceholderService.getPostsByUserId(1L)).thenReturn(Mono.just(userPosts));
        when(documentService.renderPostsToHtml(userPosts)).thenReturn(Mono.just(html));
        when(documentService.convertHtmlToFormat(html, "docx")).thenReturn(Mono.just(docxBytes));

        // Act & Assert
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/posts/document")
                        .queryParam("format", "docx")
                        .queryParam("userId", "1")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                .expectBody(byte[].class).isEqualTo(docxBytes);

        verify(jsonPlaceholderService, times(1)).getPostsByUserId(1L);
        verify(documentService, times(1)).renderPostsToHtml(userPosts);
        verify(documentService, times(1)).convertHtmlToFormat(html, "docx");
    }

    @Test
    public void generateDocument_WithRtfFormatAndPostId_ShouldReturnRtfDocument() {
        // Arrange
        Post post = new Post(1L, 1L, "Test Post", "This is a test post");
        List<Post> singlePost = Collections.singletonList(post);
        String html = "<html><body><h1>Single Post</h1></body></html>";
        byte[] rtfBytes = "RTF content".getBytes();

        when(jsonPlaceholderService.getPostById(1L)).thenReturn(Mono.just(post));
        when(documentService.renderPostsToHtml(singlePost)).thenReturn(Mono.just(html));
        when(documentService.convertHtmlToFormat(html, "rtf")).thenReturn(Mono.just(rtfBytes));

        // Act & Assert
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/posts/document")
                        .queryParam("format", "rtf")
                        .queryParam("postId", "1")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.parseMediaType("application/rtf"))
                .expectBody(byte[].class).isEqualTo(rtfBytes);

        verify(jsonPlaceholderService, times(1)).getPostById(1L);
        verify(documentService, times(1)).renderPostsToHtml(singlePost);
        verify(documentService, times(1)).convertHtmlToFormat(html, "rtf");
    }

    @Test
    public void generateDocument_WhenNoPostsFound_ShouldReturnNotFound() {
        // Arrange
        when(jsonPlaceholderService.getPostById(999L)).thenReturn(Mono.just(new Post()));

        // Act & Assert
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/posts/document")
                        .queryParam("postId", "999")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();

        verify(jsonPlaceholderService, times(1)).getPostById(999L);
        verify(documentService, never()).renderPostsToHtml(any());
        verify(documentService, never()).convertHtmlToFormat(any(), any());
    }
}