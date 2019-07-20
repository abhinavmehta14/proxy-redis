package com.amehta.proxy.redis.interact;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import com.amehta.proxy.redis.interact.health.TemplateHealthCheck;

import java.util.concurrent.TimeUnit;


public class RedisApplication extends Application<RedisConfiguration> {

    @Override
    public String getName() {
        return "redis-app";
    }

    @Override
    public void initialize(Bootstrap<RedisConfiguration> bootstrap) {
    }

    @Override
    public void run(RedisConfiguration configuration,
                    Environment environment) {


        final RedisAppResource resource = new RedisAppResource(
                configuration.getRedisAddress(),
                configuration.getRedisPort(),
                configuration.getJedisPoolSize(),
                configuration.getCacheSize(),
                configuration.getCacheTimeout()
        );

        final TemplateHealthCheck healthCheck =
                new TemplateHealthCheck();

        environment.healthChecks().register("template", healthCheck);
        environment.jersey().register(resource);
    }

    public static void main(String[] args) throws Exception {
        new RedisApplication().run(args);
    }

}