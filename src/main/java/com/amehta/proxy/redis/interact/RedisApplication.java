package com.amehta.proxy.redis.interact;

import com.amehta.proxy.redis.interact.health.TemplateHealthCheck;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPool;


public class RedisApplication extends Application<RedisConfiguration> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisApplication.class);

    public static void main(String[] args) throws Exception {
        new RedisApplication().run(args);
    }

    @Override
    public String getName() {
        return "redis-app";
    }

    @Override
    public void initialize(Bootstrap<RedisConfiguration> bootstrap) {
    }

    private JedisPool getJedisPool(String redisAddress, int redisPort, int jedisPoolSize) {
        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        config.setMaxTotal(jedisPoolSize);
        return new JedisPool(config, redisAddress, redisPort);
    }

    @Override
    public void run(RedisConfiguration configuration,
                    Environment environment) {

        String redisAddress = configuration.getRedisAddress();
        int redisPort = configuration.getRedisPort();
        int jedisReadPoolSize = configuration.getJedisReadPoolSize();
        int jedisWritePoolSize = configuration.getJedisWritePoolSize();
        int cacheSize = configuration.getCacheSize();
        int cacheTimeout = configuration.getCacheTimeout();
        int cacheConcurrency = configuration.getCacheConcurrency();
        int globalExpiry = configuration.getGlobalExpiry();

        JedisPool jedisPool = getJedisPool(redisAddress, redisPort, jedisReadPoolSize);
        // jedisPoolSize for writes to be setup and read from config file separately
        CachedRedisService cachedRedisService = new CachedRedisService(
                jedisPool,
                cacheSize,
                cacheTimeout,
                cacheConcurrency
        );

        JedisPool jedisWritePool = getJedisPool(redisAddress, redisPort, jedisWritePoolSize); // TODO: read pool size from config

        final RedisAppResource resource = new RedisAppResource(
                cachedRedisService,
                jedisWritePool,
                globalExpiry
        );

        final TemplateHealthCheck healthCheck =
                new TemplateHealthCheck();

        environment.healthChecks().register("template", healthCheck);
        environment.jersey().register(resource);
    }

}