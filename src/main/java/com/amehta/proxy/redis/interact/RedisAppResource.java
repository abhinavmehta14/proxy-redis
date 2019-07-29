package com.amehta.proxy.redis.interact;

import com.codahale.metrics.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.concurrent.ExecutionException;

import static java.lang.String.format;


@Path("/v1.0/proxy/")
@Produces(MediaType.APPLICATION_JSON)
public class RedisAppResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisAppResource.class);

    private CachedRedisServiceManager cachedRedisServiceManager;
    private JedisPool jedisWritePool;
    private int globalExpiry;

    public RedisAppResource(CachedRedisServiceManager cachedRedisServiceManager, JedisPool jedisWritePool, int globalExpiry) {
        this.cachedRedisServiceManager = cachedRedisServiceManager;
        this.jedisWritePool = jedisWritePool;
        this.globalExpiry = globalExpiry;
    }

    private String getValueFromCache(String key) throws ExecutionException {
        return cachedRedisServiceManager.getValue(key).orElse(null);
    }

    @GET
    @Timed
    public Response get(@QueryParam("key") String key) throws ExecutionException {
        String val;
        if (key == null)
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("key is null")
                    .build();


        val = getValueFromCache(key);

        if (val == null)
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity("key not found")
                    .build();

        return Response.ok()
                .entity(val)
                .build();
    }


    @POST
    public Response post(@QueryParam("key") String key, @QueryParam("value") String value) {
        if (key == null || value == null)
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(format("either key or value is null. key=%s, value=%s", key, value))
                    .build();

        String response;
        LOGGER.info(format("performing db write for key=%s, value=%s", key, value));
        try (Jedis resource = this.jedisWritePool.getResource()) {
            response = resource.set(key, value);
            long dontCare = resource.expire(key, this.globalExpiry); // ignoring value for now; TODO: add checks based on api contract
        }
        cachedRedisServiceManager.invalidateCache(key);

        if ("OK".equals(response)) {
            return Response
                    .status(Response.Status.OK)
                    .entity(format("key=%s, value=%s set successfully", key, value))
                    .build();
        } else {
            return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(format("something went wrong while setting key=%s, value=%s. redis response=%s", key, value, response))
                    .build();
        }
    }

}