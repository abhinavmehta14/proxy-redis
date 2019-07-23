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

    @NotNull
    private Integer globalExpiry;

    @NotEmpty
    private String redisAddress;

    @NotNull
    private Integer redisPort;

    @NotNull
    private Integer jedisReadPoolSize;

    @NotNull
    private Integer jedisWritePoolSize;

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
    public int getGlobalExpiry() {
        return this.globalExpiry;
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
    public int getJedisReadPoolSize() {
        return this.jedisReadPoolSize;
    }

    @JsonProperty
    public int getJedisWritePoolSize() {
        return this.jedisReadPoolSize;
    }

    @JsonProperty
    public int getThreadPoolSize() {
        return this.threadPoolSize;
    }

    // TODO: setters - might be useful with tests
}