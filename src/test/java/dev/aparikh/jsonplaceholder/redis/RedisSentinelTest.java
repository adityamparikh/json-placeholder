package dev.aparikh.jsonplaceholder.redis;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisSentinelPool;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

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
        // since the Sentinel might resolve to internal container IP
        try (Jedis jedis = new Jedis("localhost", redisMaster.getMappedPort(REDIS_PORT))) {
            // Test basic Redis operations
            jedis.set("testkey", "testvalue");
            String value = jedis.get("testkey");

            // Verify the value was stored and retrieved correctly
            assertNotNull(value);
            assertEquals("testvalue", value);

            System.out.println("Successfully connected to Redis and performed operations!");
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

            System.out.println("Successfully connected to Redis Sentinel and verified master configuration!");
        }
    }
}
