package com.amehta.proxy.redis;

import io.dropwizard.lifecycle.Managed;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPool;

/*
  Creating managed objects for graceful shutdown https://www.dropwizard.io/1.0.5/docs/manual/core.html#managed-objects

  If JedisPoolManager#start() throws an exception–e.g., an error connecting to the server–your application will not start and a full exception will be logged. If JedisPoolManager#stop() throws an exception, the exception will be logged but your application will still be able to shut down.

 */
public class JedisPoolManager implements Managed {

    private static Logger LOGGER = LoggerFactory.getLogger(JedisPoolManager.class);

    private String redisAddress;
    private int redisPort;
    private int jedisPoolSize;
    private JedisPool jedisPool;


    public JedisPoolManager(String redisAddress, int redisPort, int jedisPoolSize) {
        this.redisAddress = redisAddress;
        this.redisPort = redisPort;
        this.jedisPoolSize = jedisPoolSize;
    }

    @Override
    public void start() {
        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        config.setMaxTotal(this.jedisPoolSize);
        this.jedisPool = new JedisPool(config, this.redisAddress, this.redisPort);
    }

    @Override
    public void stop() {
        LOGGER.info("shutting down jedis pool gracefully");
        this.jedisPool.close();
    }

    public JedisPool getJedisPool() {
        return this.jedisPool;
    }
}
