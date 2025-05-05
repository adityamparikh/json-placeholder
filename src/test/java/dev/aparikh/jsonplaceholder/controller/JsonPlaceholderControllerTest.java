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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class JsonPlaceholderControllerTest {

    private MockMvc mockMvc;

    @Mock
    private JsonPlaceholderService jsonPlaceholderService;

    @Mock
    private DocumentService documentService;

    @InjectMocks
    private JsonPlaceholderController controller;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    public void getAllPosts_ShouldReturnAllPosts() throws Exception {
        // Arrange
        List<Post> posts = Arrays.asList(
                new Post(1L, 1L, "Test Post 1", "This is test post 1"),
                new Post(2L, 1L, "Test Post 2", "This is test post 2")
        );
        when(jsonPlaceholderService.getAllPosts()).thenReturn(posts);

        // Act & Assert
        mockMvc.perform(get("/api/posts")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].id", is(1)))
                .andExpect(jsonPath("$.data[0].title", is("Test Post 1")))
                .andExpect(jsonPath("$.data[1].id", is(2)))
                .andExpect(jsonPath("$.data[1].title", is("Test Post 2")));

        verify(jsonPlaceholderService, times(1)).getAllPosts();
    }

    @Test
    public void getPostById_WhenPostExists_ShouldReturnPost() throws Exception {
        // Arrange
        Post post = new Post(1L, 1L, "Test Post", "This is a test post");
        when(jsonPlaceholderService.getPostById(1L)).thenReturn(Optional.of(post));

        // Act & Assert
        mockMvc.perform(get("/api/posts/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.id", is(1)))
                .andExpect(jsonPath("$.data.title", is("Test Post")))
                .andExpect(jsonPath("$.data.body", is("This is a test post")));

        verify(jsonPlaceholderService, times(1)).getPostById(1L);
    }

    @Test
    public void getPostById_WhenPostDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(jsonPlaceholderService.getPostById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/posts/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.message", containsString("Post not found")));

        verify(jsonPlaceholderService, times(1)).getPostById(999L);
    }

    @Test
    public void getPostsByUserId_ShouldReturnUserPosts() throws Exception {
        // Arrange
        List<Post> userPosts = Arrays.asList(
                new Post(1L, 1L, "User Post 1", "This is user post 1"),
                new Post(2L, 1L, "User Post 2", "This is user post 2")
        );
        when(jsonPlaceholderService.getPostsByUserId(1L)).thenReturn(userPosts);

        // Act & Assert
        mockMvc.perform(get("/api/posts/user/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].userId", is(1)))
                .andExpect(jsonPath("$.data[0].title", is("User Post 1")))
                .andExpect(jsonPath("$.data[1].userId", is(1)))
                .andExpect(jsonPath("$.data[1].title", is("User Post 2")));

        verify(jsonPlaceholderService, times(1)).getPostsByUserId(1L);
    }

    @Test
    public void getGenericData_ShouldReturnGenericData() throws Exception {
        // Arrange
        Map<String, Object> userData = new HashMap<>();
        userData.put("id", 1);
        userData.put("name", "John Doe");
        userData.put("email", "john.doe@example.com");

        when(jsonPlaceholderService.getForObject(eq("/users"), eq(Object.class))).thenReturn(userData);

        // Act & Assert
        mockMvc.perform(get("/api/posts/generic/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.id", is(1)))
                .andExpect(jsonPath("$.data.name", is("John Doe")))
                .andExpect(jsonPath("$.data.email", is("john.doe@example.com")));

        verify(jsonPlaceholderService, times(1)).getForObject(eq("/users"), eq(Object.class));
    }

    @Test
    public void generateDocument_WithPdfFormat_ShouldReturnPdfDocument() throws Exception {
        // Arrange
        List<Post> posts = Arrays.asList(
                new Post(1L, 1L, "Test Post 1", "This is test post 1"),
                new Post(2L, 1L, "Test Post 2", "This is test post 2")
        );
        String html = "<html><body><h1>Posts</h1></body></html>";
        byte[] pdfBytes = "PDF content".getBytes();

        when(jsonPlaceholderService.getAllPosts()).thenReturn(posts);
        when(documentService.renderPostsToHtml(posts)).thenReturn(html);
        when(documentService.convertHtmlToFormat(html, "pdf")).thenReturn(pdfBytes);

        // Act & Assert
        mockMvc.perform(get("/api/posts/document")
                .param("format", "pdf")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(content().bytes(pdfBytes));

        verify(jsonPlaceholderService, times(1)).getAllPosts();
        verify(documentService, times(1)).renderPostsToHtml(posts);
        verify(documentService, times(1)).convertHtmlToFormat(html, "pdf");
    }

    @Test
    public void generateDocument_WithDocxFormatAndUserId_ShouldReturnDocxDocument() throws Exception {
        // Arrange
        List<Post> userPosts = Arrays.asList(
                new Post(1L, 1L, "User Post 1", "This is user post 1"),
                new Post(2L, 1L, "User Post 2", "This is user post 2")
        );
        String html = "<html><body><h1>User Posts</h1></body></html>";
        byte[] docxBytes = "DOCX content".getBytes();

        when(jsonPlaceholderService.getPostsByUserId(1L)).thenReturn(userPosts);
        when(documentService.renderPostsToHtml(userPosts)).thenReturn(html);
        when(documentService.convertHtmlToFormat(html, "docx")).thenReturn(docxBytes);

        // Act & Assert
        mockMvc.perform(get("/api/posts/document")
                .param("format", "docx")
                .param("userId", "1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document")))
                .andExpect(content().bytes(docxBytes));

        verify(jsonPlaceholderService, times(1)).getPostsByUserId(1L);
        verify(documentService, times(1)).renderPostsToHtml(userPosts);
        verify(documentService, times(1)).convertHtmlToFormat(html, "docx");
    }

    @Test
    public void generateDocument_WithRtfFormatAndPostId_ShouldReturnRtfDocument() throws Exception {
        // Arrange
        Post post = new Post(1L, 1L, "Test Post", "This is a test post");
        List<Post> singlePost = Collections.singletonList(post);
        String html = "<html><body><h1>Single Post</h1></body></html>";
        byte[] rtfBytes = "RTF content".getBytes();

        when(jsonPlaceholderService.getPostById(1L)).thenReturn(Optional.of(post));
        when(documentService.renderPostsToHtml(singlePost)).thenReturn(html);
        when(documentService.convertHtmlToFormat(html, "rtf")).thenReturn(rtfBytes);

        // Act & Assert
        mockMvc.perform(get("/api/posts/document")
                .param("format", "rtf")
                .param("postId", "1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.parseMediaType("application/rtf")))
                .andExpect(content().bytes(rtfBytes));

        verify(jsonPlaceholderService, times(1)).getPostById(1L);
        verify(documentService, times(1)).renderPostsToHtml(singlePost);
        verify(documentService, times(1)).convertHtmlToFormat(html, "rtf");
    }

    @Test
    public void generateDocument_WhenNoPostsFound_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(jsonPlaceholderService.getPostById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/posts/document")
                .param("postId", "999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(jsonPlaceholderService, times(1)).getPostById(999L);
        verify(documentService, never()).renderPostsToHtml(any());
        verify(documentService, never()).convertHtmlToFormat(any(), any());
    }

    @Test
    public void generateDocument_WithUnsupportedFormat_ShouldReturnError() throws Exception {
        // Arrange
        List<Post> posts = Arrays.asList(
                new Post(1L, 1L, "Test Post 1", "This is test post 1")
        );
        String html = "<html><body><h1>Posts</h1></body></html>";

        when(jsonPlaceholderService.getAllPosts()).thenReturn(posts);
        when(documentService.renderPostsToHtml(posts)).thenReturn(html);
        when(documentService.convertHtmlToFormat(html, "unsupported"))
                .thenThrow(new IllegalArgumentException("Unsupported format: unsupported"));

        // Act & Assert
        mockMvc.perform(get("/api/posts/document")
                .param("format", "unsupported")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(jsonPlaceholderService, times(1)).getAllPosts();
        verify(documentService, times(1)).renderPostsToHtml(posts);
        verify(documentService, times(1)).convertHtmlToFormat(html, "unsupported");
    }
}
