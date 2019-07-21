package com.amehta.proxy.redis.interact;

import com.codahale.metrics.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.concurrent.ExecutionException;


@Path("/v1.0/proxy/")
@Produces(MediaType.APPLICATION_JSON)
public class RedisAppResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisAppResource.class);

    private CachedRedisService cachedRedisService;

    public RedisAppResource(CachedRedisService cachedRedisService) {
        this.cachedRedisService = cachedRedisService;
    }

    private String getValueFromCache(String key) throws ExecutionException {
        return cachedRedisService.getValue(key).orElse(null);
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
}