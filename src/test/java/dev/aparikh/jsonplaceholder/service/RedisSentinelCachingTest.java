package dev.aparikh.jsonplaceholder.service;

import dev.aparikh.jsonplaceholder.model.Post;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@Testcontainers
public class RedisSentinelCachingTest {

    private static final String REDIS_IMAGE = "bitnami/redis:latest";
    private static final int REDIS_PORT = 6379;

    @Container
    private static final GenericContainer<?> redisContainer = new GenericContainer<>(DockerImageName.parse(REDIS_IMAGE))
            .withExposedPorts(REDIS_PORT)
            .withEnv("ALLOW_EMPTY_PASSWORD", "yes");

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", () -> redisContainer.getMappedPort(REDIS_PORT));
        // Disable sentinel for this test
        registry.add("spring.data.redis.sentinel.master", () -> "");
        registry.add("spring.data.redis.sentinel.nodes", () -> "");
        // Enable Redis cache
        registry.add("spring.cache.type", () -> "redis");
    }

    @TestConfiguration
    @EnableCaching
    static class TestConfig {
        @Bean
        @Primary
        public RestClient jsonPlaceholderRestClient() {
            return mock(RestClient.class);
        }

        @Bean
        @Primary
        public RedisConnectionFactory redisConnectionFactory() {
            RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration(
                    redisContainer.getHost(),
                    redisContainer.getMappedPort(REDIS_PORT)
            );
            return new LettuceConnectionFactory(redisConfig);
        }
    }

    @Autowired
    private JsonPlaceholderService jsonPlaceholderService;

    @Autowired
    private RestClient restClient;

    @BeforeEach
    void setUp() {
        reset(restClient);
    }

    @Test
    void testGetPostByIdCaching() {
        // Arrange
        Long postId = 1L;
        Post expectedPost = new Post(postId, 1L, "Test Title", "Test Body");

        // Setup mock chain
        RestClient.RequestHeadersUriSpec<?> requestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.RequestHeadersSpec<?> requestHeadersSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        // Setup the mock chain behavior
        doReturn(requestHeadersUriSpec).when(restClient).get();
        doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri(anyString(), any(Object.class));
        doReturn(responseSpec).when(requestHeadersSpec).retrieve();
        doReturn(expectedPost).when(responseSpec).body(Post.class);

        // Act - First call should hit the API
        Optional<Post> result1 = jsonPlaceholderService.getPostById(postId);

        // Act - Second call should be from cache
        Optional<Post> result2 = jsonPlaceholderService.getPostById(postId);

        // Assert
        assertTrue(result1.isPresent());
        assertEquals(expectedPost.getId(), result1.get().getId());
        assertEquals(expectedPost.getTitle(), result1.get().getTitle());

        assertTrue(result2.isPresent());
        assertEquals(expectedPost.getId(), result2.get().getId());
        assertEquals(expectedPost.getTitle(), result2.get().getTitle());

        // Verify that the API was called only once
        verify(restClient, times(1)).get();
    }

    @Test
    void testGetAllPostsCaching() {
        // Arrange
        List<Post> expectedPosts = List.of(
            new Post(1L, 1L, "Title 1", "Body 1"),
            new Post(2L, 1L, "Title 2", "Body 2")
        );

        // Setup mock chain
        RestClient.RequestHeadersUriSpec<?> requestHeadersUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.RequestHeadersSpec<?> requestHeadersSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        // Setup the mock chain behavior
        doReturn(requestHeadersUriSpec).when(restClient).get();
        doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri("/posts");
        doReturn(responseSpec).when(requestHeadersSpec).retrieve();
        doReturn(expectedPosts).when(responseSpec).body(any(ParameterizedTypeReference.class));

        // Act - First call should hit the API
        List<Post> result1 = jsonPlaceholderService.getAllPosts();

        // Act - Second call should be from cache
        List<Post> result2 = jsonPlaceholderService.getAllPosts();

        // Assert
        assertEquals(2, result1.size());
        assertEquals(expectedPosts.get(0).getId(), result1.get(0).getId());
        assertEquals(expectedPosts.get(1).getId(), result1.get(1).getId());

        assertEquals(2, result2.size());
        assertEquals(expectedPosts.get(0).getId(), result2.get(0).getId());
        assertEquals(expectedPosts.get(1).getId(), result2.get(1).getId());

        // Verify that the API was called only once
        verify(restClient, times(1)).get();
    }
}
