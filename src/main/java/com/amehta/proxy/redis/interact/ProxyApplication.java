package com.amehta.proxy.redis.interact;

import com.amehta.proxy.redis.JedisPoolManager;
import com.amehta.proxy.redis.interact.health.TemplateHealthCheck;
import com.amehta.proxy.redis.interact.test.ProxyLoadTestCommand;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPool;


public class ProxyApplication extends Application<RedisConfiguration> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyApplication.class);

    public static void main(String[] args) throws Exception {
        new ProxyApplication().run(args);
    }

    @Override
    public String getName() {
        return "redis-app";
    }

    @Override
    public void initialize(Bootstrap<RedisConfiguration> bootstrap) {
        bootstrap.addCommand(new ProxyLoadTestCommand());
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
        // TODO: Make this a Managed service
        CachedRedisServiceManager cachedRedisServiceManager = new CachedRedisServiceManager(
                jedisPool,
                cacheSize,
                cacheTimeout,
                cacheConcurrency
        );

        JedisPoolManager jedisPoolManager = new JedisPoolManager(redisAddress, redisPort, jedisWritePoolSize);
        JedisPool jedisWritePool = jedisPoolManager.getJedisPool();

        final RedisAppResource resource = new RedisAppResource(
                cachedRedisServiceManager,
                jedisWritePool,
                globalExpiry
        );

        final TemplateHealthCheck healthCheck =
                new TemplateHealthCheck();

        LOGGER.info("registering managed objects");
        environment.lifecycle().manage(jedisPoolManager);
        environment.lifecycle().manage(cachedRedisServiceManager);
        LOGGER.info("registered managed objects");
        environment.healthChecks().register("template", healthCheck);
        environment.jersey().register(resource);
    }

}