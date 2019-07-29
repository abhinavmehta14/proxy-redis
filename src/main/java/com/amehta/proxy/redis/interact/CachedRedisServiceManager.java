package com.amehta.proxy.redis.interact;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.CacheStats;
import com.google.common.cache.LoadingCache;
import io.dropwizard.lifecycle.Managed;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;

public class CachedRedisServiceManager implements Managed {

    private static final Logger LOGGER = LoggerFactory.getLogger(CachedRedisServiceManager.class);
    private JedisPool jedisPool;
    private LoadingCache<String, Optional<String>> cache;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private volatile CacheStats lastStats;

    public CachedRedisServiceManager(JedisPool jedisPool, int cacheSize, int cacheTimeout, int cacheConcurrency) {
        this.jedisPool = jedisPool;
        this.cache = getCache(cacheSize, cacheTimeout, cacheConcurrency);
        this.lastStats = cache.stats();
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
                .recordStats()
                .build(loader);
    }

    private String getValueFromRedis(String key) {
        try (Jedis connection = jedisPool.getResource()) {
            return connection.get(key);  // case sensitive lookup
        }
    }

    public Optional<String> getValue(String key) throws ExecutionException {
        long stTime = DateTime.now().getMillis();
        Optional<String> s = cache.get(key);
        long endTime = DateTime.now().getMillis();
        long tt = endTime - stTime;
        if(tt > 10)
            LOGGER.warn(format("slow cache.get tt=%d", endTime - stTime));
        return s;
    }

    public void invalidateCache(String key) {
        LOGGER.info(format("invalidating guava cache for key=%s", key));
        cache.invalidate(key);
        LOGGER.info(format("invalidated guava cache for key=%s", key));
    }

    @Override
    public void start() {
        LOGGER.info("starting...");
        this.scheduler.scheduleAtFixedRate(() -> {
            try {
                final CacheStats oldStats = lastStats;
                lastStats = cache.stats();
                LOGGER.info("cache stats {}", lastStats.minus(oldStats));
            } catch (Exception e) {
                LOGGER.error("Could not get {} stats", e);
            }}, 1, 1, TimeUnit.MINUTES);
        LOGGER.info("started");
    }


    @Override
    public void stop() {
        LOGGER.info("shutting down...");
        scheduler.shutdown();
        LOGGER.info("shutdown complete");
    }

}
