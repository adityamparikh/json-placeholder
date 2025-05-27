# Redis Sentinel with Testcontainers for Caching Tests

This document provides guidance on how to use Redis Sentinel with Testcontainers for caching tests in Spring Boot applications.

## Overview

Redis Sentinel provides high availability for Redis by monitoring Redis instances, providing automatic failover, and serving as a configuration provider for clients. Using Redis Sentinel with Testcontainers allows us to test our application's caching functionality in an environment that closely resembles production.

## Implementation

We've implemented a basic Redis Sentinel setup using Testcontainers. The implementation consists of:

1. A Redis master container
2. A Redis Sentinel container that monitors the master
3. A test class that verifies the Redis Sentinel setup works correctly

## Dependencies

Add the following dependencies to your `build.gradle.kts` file:

```kotlin
// Testcontainers dependencies
val testcontainersVersion = "1.19.7"
testImplementation("org.testcontainers:junit-jupiter:${testcontainersVersion}")
testImplementation("org.testcontainers:testcontainers:${testcontainersVersion}")

// Jedis for Redis client
testImplementation("redis.clients:jedis:5.1.0")
```

## Redis Sentinel Test

The `RedisSentinelTest` class demonstrates how to set up Redis Sentinel with Testcontainers and verify that it works correctly:

```java
@Testcontainers
public class RedisSentinelTest {

    private static final String REDIS_IMAGE = "bitnami/redis:latest";
    private static final String REDIS_SENTINEL_IMAGE = "bitnami/redis-sentinel:latest";
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
    private static final GenericContainer<?> redisSentinel = new GenericContainer<>(DockerImageName.parse(REDIS_SENTINEL_IMAGE))
            .withNetwork(NETWORK)
            .withNetworkAliases("redis-sentinel")
            .withExposedPorts(SENTINEL_PORT)
            .withEnv("REDIS_SENTINEL_DOWN_AFTER_MILLISECONDS", "5000")
            .withEnv("REDIS_MASTER_HOST", "redis-master")
            .withEnv("REDIS_MASTER_PORT_NUMBER", String.valueOf(REDIS_PORT))
            .withEnv("REDIS_MASTER_SET", MASTER_NAME)
            .withEnv("ALLOW_EMPTY_PASSWORD", "yes");

    @Test
    void testRedisSentinelConnection() {
        // For testing purposes, we'll connect directly to Redis master
        try (Jedis jedis = new Jedis("localhost", redisMaster.getMappedPort(REDIS_PORT))) {
            // Test basic Redis operations
            jedis.set("testkey", "testvalue");
            String value = jedis.get("testkey");
            
            // Verify the value was stored and retrieved correctly
            assertNotNull(value);
            assertEquals("testvalue", value);
        }
        
        // Now verify that Sentinel is working by getting info about the master
        try (Jedis sentinelJedis = new Jedis("localhost", redisSentinel.getMappedPort(SENTINEL_PORT))) {
            // Get master info from sentinel
            List<Map<String, String>> masters = sentinelJedis.sentinelMasters();
            
            // Verify we have at least one master
            assertNotNull(masters);
            assertTrue(masters.size() > 0);
            
            // Verify the master name
            Map<String, String> masterInfo = masters.get(0);
            assertEquals(MASTER_NAME, masterInfo.get("name"));
        }
    }
}
```

## Integration with Spring Boot

To integrate Redis Sentinel with Spring Boot for caching tests, you would typically:

1. Set up Redis Sentinel containers as shown above
2. Configure Spring Boot to use Redis Sentinel for caching
3. Write tests that verify caching behavior

However, integrating Redis Sentinel with Spring Boot for testing can be complex due to networking issues between the test environment and the containers. A simpler approach for testing caching functionality is to use a standalone Redis instance:

```java
@SpringBootTest
@Testcontainers
public class RedisCachingTest {

    @Container
    private static final GenericContainer<?> redisContainer = new GenericContainer<>(DockerImageName.parse("bitnami/redis:latest"))
            .withExposedPorts(6379)
            .withEnv("ALLOW_EMPTY_PASSWORD", "yes");

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", () -> redisContainer.getMappedPort(6379));
        registry.add("spring.cache.type", () -> "redis");
    }

    // Test methods that verify caching behavior
}
```

## Conclusion

Redis Sentinel with Testcontainers provides a powerful way to test Redis-based caching in a controlled environment. While integrating it with Spring Boot for testing can be challenging, the approach demonstrated in this document provides a foundation for implementing Redis Sentinel testing in your application.