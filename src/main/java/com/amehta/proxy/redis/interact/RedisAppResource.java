package com.amehta.proxy.redis.interact;

import com.amehta.proxy.redis.interact.api.KeyVal;
import com.codahale.metrics.annotation.Timed;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;


@Path("/v1.0/proxy/")
@Produces(MediaType.APPLICATION_JSON)
public class RedisAppResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisAppResource.class);

    private final JedisPool jedisPool;
    private LoadingCache<String, Optional<String>> cache;

    private JedisPool getJedisPool(String redisAddress, int redisPort, int jedisPoolSize) {
        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        config.setMaxTotal(jedisPoolSize);
        return new JedisPool(config, redisAddress, redisPort);

        // return new JedisPool(redisAddress, redisPort);
    }

    /* Initialize a guava cache with below params from config
        - maximum size
        - key expiry time
    */
    private LoadingCache<String, Optional<String>> getCache(int cacheSize, int cacheTimeout) {
        CacheLoader<String, Optional<String>> loader = new CacheLoader<String, Optional<String>>() {
            @Override
            public Optional<String> load(String key) {
                // in case of a key miss fallback on redis
                LOGGER.info(format("cache miss for key=%s", key));
                return Optional.ofNullable(getValueFromRedis(key));
            }
        };

        // TODO: Setup cache to periodically dump cache stats - hits, misses
        return CacheBuilder
                .newBuilder()
                .maximumSize(cacheSize)
                .expireAfterWrite(cacheTimeout, TimeUnit.SECONDS)
                .build(loader);
    }

    private String getValueFromRedis(String key) {
        try(Jedis connection = RedisAppResource.this.jedisPool.getResource()) {
            return connection.get(key);  // case sensitive lookup
        }
    }

    public RedisAppResource(String redisAddress, int redisPort, int jedisPoolSize, int cacheSize, int cacheTimeout) {
        this.jedisPool = getJedisPool(redisAddress, redisPort, jedisPoolSize);
        this.cache = getCache(cacheSize, cacheTimeout);
    }

    private String getValueFromCache(String key) throws ExecutionException {
        return cache.get(key).orElse(null);
    }

    @GET
    @Timed
    public Response get(@QueryParam("key") String key) throws ExecutionException {
        String val;
        if(key == null)
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("key is null")
                    .build();

        val = getValueFromCache(key);

        if(val == null)
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity("key not found")
                    .build();

        return Response.ok()
                .entity(new KeyVal(key, val))
                .build();
    }
}