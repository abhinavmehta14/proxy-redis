package com.amehta.proxy.redis.interact;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

public class RedisConfiguration extends Configuration {

    @NotNull
    private Integer cacheSize;

    @NotNull
    private Integer cacheConcurrency;

    @NotNull
    private Integer cacheTimeout;

    @NotEmpty
    private String redisAddress;

    @NotNull
    private Integer redisPort;

    @NotNull
    private Integer jedisPoolSize;

    @NotNull
    private Integer threadPoolSize;

    @JsonProperty
    public int getCacheSize() {
        return this.cacheSize;
    }

    @JsonProperty
    public int getCacheConcurrency() {
        return this.cacheConcurrency;
    }

    @JsonProperty
    public int getCacheTimeout() {
        return this.cacheTimeout;
    }

    @JsonProperty
    public String getRedisAddress() {
        return redisAddress;
    }

    @JsonProperty
    public int getRedisPort() {
        return redisPort;
    }

    @JsonProperty
    public int getJedisPoolSize() {
        return this.threadPoolSize;
    }

    @JsonProperty
    public int getThreadPoolSize() {
        return threadPoolSize;
    }

    // TODO: setters - might be useful with tests
}