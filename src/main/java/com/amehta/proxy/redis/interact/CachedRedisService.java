package com.amehta.proxy.redis.interact;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;

public class CachedRedisService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CachedRedisService.class);
    private JedisPool jedisPool;
    private LoadingCache<String, Optional<String>> cache;

    public CachedRedisService(JedisPool jedisPool, int cacheSize, int cacheTimeout, int cacheConcurrency) {
        this.jedisPool = jedisPool;
        this.cache = getCache(cacheSize, cacheTimeout, cacheConcurrency);
    }

    /* Initialize a guava cache with below params from config
       1. maximum size
       2. key expiry time
       3. concurrency level to parallelize writes
      TODO: Any redis insert or update should invalidate cache
      Note that the cache is setup to cache the knowledge that a key is missing
   */
    LoadingCache<String, Optional<String>> getCache(int cacheSize, int cacheTimeout, int cacheConcurrency) {
        CacheLoader<String, Optional<String>> loader = new CacheLoader<String, Optional<String>>() {
            @Override
            public Optional<String> load(String key) {
                // in case of a key miss fallback on redis
                LOGGER.info(format("cache miss for key=%s", key));
                String value = getValueFromRedis(key);
                return Optional.ofNullable(value);
            }
        };

        // TODO: Setup cache to periodically dump cache stats - hits, misses
        return CacheBuilder
                .newBuilder()
                .concurrencyLevel(cacheConcurrency)
                .maximumSize(cacheSize)
                .expireAfterWrite(cacheTimeout, TimeUnit.SECONDS)
                .build(loader);
    }

    private String getValueFromRedis(String key) {
        try (Jedis connection = jedisPool.getResource()) {
            return connection.get(key);  // case sensitive lookup
        }
    }

    public Optional<String> getValue(String key) throws ExecutionException {
        return cache.get(key);
    }

    public void invalidateCache(String key) {
        LOGGER.info(format("invalidating guava cache for key=%s", key));
        cache.invalidate(key);
    }
}
