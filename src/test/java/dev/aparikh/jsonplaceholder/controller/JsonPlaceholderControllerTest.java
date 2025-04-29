package dev.aparikh.jsonplaceholder.controller;

import dev.aparikh.jsonplaceholder.model.Post;
import dev.aparikh.jsonplaceholder.service.JsonPlaceholderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class JsonPlaceholderControllerTest {

    private MockMvc mockMvc;

    @Mock
    private JsonPlaceholderService jsonPlaceholderService;

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
}
