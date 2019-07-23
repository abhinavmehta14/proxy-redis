package com.amehta.proxy.redis.interact;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;

public class RedisAppResourceIntegrationTest {

    private static Logger LOGGER = LoggerFactory.getLogger(CachedRedisServiceTest.class);

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
        resource = new RedisAppResource(cachedRedisService, null, 3600);
        pool = new JedisPool(redisAddress, redisPort);
        try (Jedis resource = pool.getResource()) {
            resource.set("k1", "v1"); // TODO: set an expiry s.t. this disappers after tests run
        }
        initRedisStore(pool);
    }

    @After
    public void tearDown() {
        pool.close();
    }

    private void initRedisStore(JedisPool pool) {
        // add key values you would like to initialize DB with
        // Note that key k1 is reserved for tests. Do not mutate that here

        Map<String, String> preLoadedKeyValues = new HashMap<String, String>() {{
            // add more key values to meet your requirements
            put("a1", "b1");
            put("a2", "b2");
            put("a3", "b3");
            put("a4", "b4");
        }};
        try (Jedis resource = pool.getResource()) {
            for (Map.Entry<String, String> kv : preLoadedKeyValues.entrySet()) {
                LOGGER.info(format("initializing redis with key=%s value=%s", kv.getKey(), kv.getValue()));
                String resp = resource.set(kv.getKey(), kv.getValue());
                if (!"OK".equals(resp))
                    throw new RuntimeException(format("Bad response=%s while initializing DB", resp));
                // TODO: 1. read globalExpiry from config 2. check if expiry is set as desired
                resource.expire(kv.getKey(), 3600);
            }
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
