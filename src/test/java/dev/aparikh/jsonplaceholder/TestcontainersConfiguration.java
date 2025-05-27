package dev.aparikh.jsonplaceholder;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    private static final String REDIS_IMAGE = "bitnami/redis:latest";
    private static final int REDIS_PORT = 6379;
    private static final int SENTINEL_PORT = 26379;
    private static final String MASTER_NAME = "mymaster";

    @Bean(destroyMethod = "stop")
    GenericContainer<?> redisContainer() {
        GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse(REDIS_IMAGE))
                .withExposedPorts(REDIS_PORT)
                .withEnv("REDIS_REPLICATION_MODE", "master")
                .withEnv("ALLOW_EMPTY_PASSWORD", "yes");
        redis.start();
        return redis;
    }

    @Bean
    public String configureRedisProperties(DynamicPropertyRegistry registry, 
                                         GenericContainer<?> redisContainer) {
        // For testing, we'll use a direct Redis connection instead of Sentinel
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", () -> redisContainer.getMappedPort(REDIS_PORT));
        // Disable Sentinel for tests
        registry.add("spring.data.redis.sentinel.master", () -> "");
        registry.add("spring.data.redis.sentinel.nodes", () -> "");
        return "redisProperties";
    }
}
