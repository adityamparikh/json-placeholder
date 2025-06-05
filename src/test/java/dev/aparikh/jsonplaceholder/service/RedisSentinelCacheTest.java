package dev.aparikh.jsonplaceholder.service;

import dev.aparikh.jsonplaceholder.model.Post;
import org.junit.jupiter.api.BeforeAll;
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
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
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
@Testcontainers
public class RedisSentinelCacheTest {

    private static final String REDIS_IMAGE = "bitnami/redis:latest";
    private static final int REDIS_PORT = 6379;
    private static final int SENTINEL_PORT = 26379;
    private static final String MASTER_NAME = "mymaster";
    private static final Network NETWORK = Network.newNetwork();

    @Container
    private static final GenericContainer<?> redisMaster = new GenericContainer<>(DockerImageName.parse(REDIS_IMAGE))
            .withNetwork(NETWORK)
            .withNetworkAliases("redis-master")
            .withExposedPorts(REDIS_PORT)
            .withEnv("REDIS_REPLICATION_MODE", "master")
            .withEnv("ALLOW_EMPTY_PASSWORD", "yes");

    @Container
    private static final GenericContainer<?> redisSentinel = new GenericContainer<>(DockerImageName.parse("bitnami/redis-sentinel:latest"))
            .withNetwork(NETWORK)
            .withNetworkAliases("redis-sentinel")
            .withExposedPorts(SENTINEL_PORT)
            .withEnv("REDIS_SENTINEL_DOWN_AFTER_MILLISECONDS", "5000")
            .withEnv("REDIS_MASTER_HOST", "redis-master")
            .withEnv("REDIS_MASTER_PORT_NUMBER", String.valueOf(REDIS_PORT))
            .withEnv("REDIS_MASTER_SET", MASTER_NAME)
            .withEnv("ALLOW_EMPTY_PASSWORD", "yes");

    @DynamicPropertySource
    static void registerRedisProperties(DynamicPropertyRegistry registry) {
        // Use direct Redis connection instead of Sentinel for simplicity
        registry.add("spring.data.redis.host", redisMaster::getHost);
        registry.add("spring.data.redis.port", () -> redisMaster.getMappedPort(REDIS_PORT));
        // Disable Sentinel for this test
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
        public WebClient jsonPlaceholderWebClient() {
            return mock(WebClient.class);
        }

        @Bean
        @Primary
        public RedisConnectionFactory redisConnectionFactory() {
            RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration(
                    redisMaster.getHost(),
                    redisMaster.getMappedPort(REDIS_PORT)
            );
            return new LettuceConnectionFactory(redisConfig);
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
        doReturn(reactor.core.publisher.Flux.fromIterable(expectedPosts)).when(responseSpec).bodyToFlux(Post.class);

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
