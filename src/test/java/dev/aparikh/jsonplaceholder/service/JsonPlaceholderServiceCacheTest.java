package dev.aparikh.jsonplaceholder.service;

import dev.aparikh.jsonplaceholder.model.Post;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.cache.type=simple",
    "spring.data.redis.sentinel.master=",
    "spring.data.redis.sentinel.nodes="
})
public class JsonPlaceholderServiceCacheTest {

    @TestConfiguration
    @EnableCaching
    static class TestConfig {
        @Bean
        @Primary
        public WebClient jsonPlaceholderWebClient() {
            return mock(WebClient.class);
        }

        @Bean
        @Primary
        public CacheManager cacheManager() {
            return new ConcurrentMapCacheManager("posts", "postsByUser", "apiData");
        }
    }

    @Autowired
    private JsonPlaceholderService jsonPlaceholderService;

    @Autowired
    private WebClient webClient;

    @BeforeEach
    void setUp() {
        reset(webClient);
    }

    @Test
    void testGetPostByIdCaching() {
        // Arrange
        Long postId = 1L;
        Post expectedPost = new Post(postId, 1L, "Test Title", "Test Body");

        // Setup mock chain
        WebClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        // Setup the mock chain behavior
        doReturn(requestHeadersUriSpec).when(webClient).get();
        doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri(anyString(), any(Object.class));
        doReturn(responseSpec).when(requestHeadersSpec).retrieve();
        doReturn(Mono.just(expectedPost)).when(responseSpec).bodyToMono(Post.class);

        // Act - First call should hit the API
        StepVerifier.create(jsonPlaceholderService.getPostById(postId))
                .expectNext(expectedPost)
                .verifyComplete();

        // Act - Second call should be from cache
        StepVerifier.create(jsonPlaceholderService.getPostById(postId))
                .expectNext(expectedPost)
                .verifyComplete();

        // Verify that the API was called only once
        verify(webClient, times(1)).get();
    }

    @Test
    void testGetAllPostsCaching() {
        // Arrange
        List<Post> expectedPosts = List.of(
            new Post(1L, 1L, "Title 1", "Body 1"),
            new Post(2L, 1L, "Title 2", "Body 2")
        );

        // Setup mock chain
        WebClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        // Setup the mock chain behavior
        doReturn(requestHeadersUriSpec).when(webClient).get();
        doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri("/posts");
        doReturn(responseSpec).when(requestHeadersSpec).retrieve();
        doReturn(Mono.just(expectedPosts)).when(responseSpec).bodyToMono(any(ParameterizedTypeReference.class));

        // Act - First call should hit the API
        StepVerifier.create(jsonPlaceholderService.getAllPosts())
                .expectNext(expectedPosts)
                .verifyComplete();

        // Act - Second call should be from cache
        StepVerifier.create(jsonPlaceholderService.getAllPosts())
                .expectNext(expectedPosts)
                .verifyComplete();

        // Verify that the API was called only once
        verify(webClient, times(1)).get();
    }
}
