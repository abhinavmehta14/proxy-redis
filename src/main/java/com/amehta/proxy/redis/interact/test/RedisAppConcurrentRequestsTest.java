package com.amehta.proxy.redis.interact.test;

import com.google.common.collect.Lists;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.*;

import static java.lang.String.format;

/*
  This class is meant to bombard GET endpoint with concurrent requests
  Working on my laptop
  TODO: Figure out how to run a class from a dropwizard based fat jar
  Use the command to dockerize this class
 */
public class RedisAppConcurrentRequestsTest {

    private static Logger LOGGER = LoggerFactory.getLogger(RedisAppConcurrentRequestsTest.class);

    private int executeGet(String url) throws IOException {
        HttpGet httpGet = new HttpGet(url);
        HttpClient client = HttpClientBuilder.create().build();
        return client
                .execute(httpGet)
                .getStatusLine()
                .getStatusCode();
    }

    /*
        Test to send concurrent request
     */
    private void testConcurrentRequests(int numRequests) throws ExecutionException, InterruptedException, TimeoutException {
        LOGGER.info(format("Running test to send %d concurrent requests to webserver", numRequests));
        ExecutorService executorService = Executors.newFixedThreadPool(8);
        List<Future<Integer>> futureCall = Lists.newArrayList();
        for (int i = 0; i < numRequests; i++) {
            futureCall.add(executorService.submit(() -> executeGet("http://127.0.0.1:8080/v1.0/proxy?key=a1")));
        }

        List<Integer> response = Lists.newArrayList();
        for (int i = 0; i < numRequests; i++) {
            int resp = futureCall.get(i).get();
            response.add(resp);
        }

        if (numRequests > response.size()) {
            throw new RuntimeException(format("%d responses but %d requests", response.size(), numRequests));
        } else {
            LOGGER.info("received response for all requests");
        }

        for (int i = 0; i < numRequests; i++) {
            if (200 != response.get(i)) {
                throw new RuntimeException(format("unexpected non 200 response code=%d", response.get(i)));
            }
        }

        LOGGER.info(format("received valid response for all %d requests", numRequests));

        executorService.shutdown();
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException, TimeoutException {
        // OOM on 100,000 requests
        new RedisAppConcurrentRequestsTest().testConcurrentRequests(10000);
    }

}
