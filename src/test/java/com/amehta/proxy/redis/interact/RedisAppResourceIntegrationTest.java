package com.amehta.proxy.redis.interact;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.ws.rs.core.Response;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

public class RedisAppResourceIntegrationTest {

    private RedisAppResource resource;
    private JedisPool pool;


    @Before
    public void setUp() {
        // TODO: receive redis address and port from docker-compose
        // below redis settings only work via docker-compose proxy-test
        String redisAddress = "redis-db";
        int redisPort = 6379;

        /*
         Use following while running tests locally connecting to redis-db container
         redisAddress = "localhost";
         redisPort = 7001;
        */
        pool = new JedisPool(redisAddress, redisPort);
        CachedRedisService cachedRedisService = new CachedRedisService(pool, 2, 1, 1);
        resource = new RedisAppResource(cachedRedisService, null);
        pool = new JedisPool(redisAddress, redisPort);
        try (Jedis resource = pool.getResource()) {
            resource.set("k1", "v1");
        }
        initRedisStore(pool);
    }

    @After
    public void tearDown() {
        pool.close();
    }

    private void initRedisStore(JedisPool pool) {
        // add key values you would like to initialize DB with
        try (Jedis resource = pool.getResource()) {
            // add more key values
            // Note that key k1 is reserved for tests. Do not mutate that here
            resource.set("a1", "b1");
            resource.set("a2", "b2");
            resource.set("a3", "b3");
            resource.set("a4", "b4");
            resource.set("a4", "b4");
        }
    }

    @Test
    public void testKeyNotFound() throws ExecutionException {
        int status = resource.get("key_not_found").getStatus();
        assertEquals(404, status);
    }

    @Test
    public void testOkResponse() throws ExecutionException {
        Response response = resource.get("k1");
        String body = (String) response.getEntity();
        assertEquals("v1", body);
    }

    @Test
    public void testBadRequest() throws ExecutionException {
        int status = resource.get(null).getStatus();
        assertEquals(400, status);
    }
}
