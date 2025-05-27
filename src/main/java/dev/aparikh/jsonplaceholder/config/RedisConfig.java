package dev.aparikh.jsonplaceholder.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Configuration class for Redis with Sentinel support.
 */
@Configuration
@EnableCaching
public class RedisConfig {

    @Value("${spring.data.redis.sentinel.master:mymaster}")
    private String master;

    @Value("${spring.data.redis.sentinel.nodes:localhost:26379}")
    private String nodes;

    /**
     * Creates a Redis connection factory configured with Redis Sentinel.
     *
     * @return A configured RedisConnectionFactory instance
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        // If master is empty, use a standalone Redis configuration
        if (master == null || master.isEmpty()) {
            return new LettuceConnectionFactory();
        }

        // Otherwise, use Sentinel configuration
        RedisSentinelConfiguration sentinelConfig = new RedisSentinelConfiguration();
        sentinelConfig.setMaster(master);

        // Parse and add sentinel nodes
        if (nodes != null && !nodes.isEmpty()) {
            String[] nodeArray = nodes.split(",");
            for (String node : nodeArray) {
                String[] hostAndPort = node.trim().split(":");
                String host = hostAndPort[0];
                int port = Integer.parseInt(hostAndPort[1]);
                sentinelConfig.sentinel(host, port);
            }
        }

        return new LettuceConnectionFactory(sentinelConfig);
    }

    /**
     * Creates a RedisTemplate bean configured for general use.
     *
     * @param connectionFactory The Redis connection factory
     * @return A configured RedisTemplate instance
     */
    @Bean
    @ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis")
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    @Value("${spring.cache.type:redis}")
    private String cacheType;

    /**
     * Creates a cache manager that tries Redis first and falls back to in-memory cache.
     *
     * @return A configured CacheManager instance
     */
    @Bean
    @Primary
    public CacheManager cacheManager() {
        Logger logger = LoggerFactory.getLogger(RedisConfig.class);
        
        // Check if Redis caching is enabled
        if (!"redis".equals(cacheType)) {
            logger.info("Redis caching disabled (cache.type={}), using in-memory cache manager", cacheType);
            return new ConcurrentMapCacheManager("posts", "postsByUser", "apiData");
        }
        
        try {
            // Try to create Redis connection factory and test connection
            RedisConnectionFactory connectionFactory = redisConnectionFactory();
            connectionFactory.getConnection().ping();
            logger.info("Redis connection successful, using Redis cache manager");
            return RedisCacheManager.builder(connectionFactory).build();
        } catch (Exception e) {
            logger.warn("Redis connection failed, falling back to in-memory cache: {}", e.getMessage());
            return new ConcurrentMapCacheManager("posts", "postsByUser", "apiData");
        }
    }
}
