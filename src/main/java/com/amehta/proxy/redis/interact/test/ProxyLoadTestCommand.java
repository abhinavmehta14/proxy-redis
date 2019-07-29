package com.amehta.proxy.redis.interact.test;

import com.google.common.collect.Lists;
import io.dropwizard.cli.Command;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

import static java.lang.String.format;

/*
  This class is meant to bombard GET endpoint with concurrent requests
  NOTE: Look for robust open source alternatives
 */

public class ProxyLoadTestCommand extends Command {

    private static final String CONCURRENCY_LEVEL_KEY = "concurrencyLevel";
    private static final String NUM_REQUEST_KEY = "numRequest";
    private static final String APP_NAME = "proxy-load-test"; // used to invoke this

    static {
        // disabling DEBUG logs for classes from the below namespaces
        Set<String> artifactoryLoggers = new HashSet<>(Arrays.asList("org.apache.http", "groovyx.net.http"));

        for (String log : artifactoryLoggers) {
            ch.qos.logback.classic.Logger artLogger = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(log);
            artLogger.setLevel(ch.qos.logback.classic.Level.INFO);
            artLogger.setAdditive(false);
        }
    }

    private static Logger LOGGER = LoggerFactory.getLogger(ProxyLoadTestCommand.class);

    public ProxyLoadTestCommand() {
        super(APP_NAME, "load test");
    }

    private int executeGet(String url) throws IOException {
        // TODO: Introduce connection keep-alive option
        int statusCode;
        try (CloseableHttpClient closeableHttpClient = HttpClientBuilder.create().build()) {
            HttpGet httpGet = new HttpGet(url);
            statusCode = closeableHttpClient
                    .execute(httpGet)
                    .getStatusLine()
                    .getStatusCode();
        }
        return statusCode;
    }

    /*
        Test to send concurrent request
     */
    private boolean testConcurrentRequests(int numRequests, int concurrenyLevel) throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(concurrenyLevel);
        // TODO: parameterize url config
        String url = "http://proxy:8080/v1.0/proxy?key=a1"; // Hitting single key
        LOGGER.info(format("Using url=%s for load test", url));
        List<Future<Integer>> futureCall = Lists.newArrayList();
        for (int i = 0; i < numRequests; i++) {
            futureCall.add(executorService.submit(() -> executeGet(url)));
        }

        List<Integer> response = Lists.newArrayList();
        int errorCount = 0;
        for (int i = 0; i < numRequests; i++) {
            try {
                int resp = futureCall.get(i).get(1, TimeUnit.SECONDS);
                response.add(resp);
            } catch (ExecutionException e) {
                errorCount++;
                LOGGER.error(format("failed to get response on request %d", i), e);
            } catch (TimeoutException e) {
                errorCount++;
                LOGGER.error(format("timeout after 1sec on request %d", i), e);
            }
        }

        if (numRequests > response.size()) {
            throw new RuntimeException(format("%d responses but %d requests", response.size(), numRequests));
        } else {
            LOGGER.info("received response for all requests");
        }

        boolean success = true;
        for (int i = 0; i < numRequests; i++) {
            if (200 != response.get(i)) {
                success = false;
                LOGGER.error(format("unexpected non 200 response code=%d", response.get(i)));
                errorCount++;
            }
        }

        if (errorCount == 0) {
            LOGGER.info(format("received valid response for all %d requests", numRequests));
        } else {
            LOGGER.error(format("non 200 response for %d of %d requests", errorCount, numRequests));
        }

        executorService.shutdown();
        return success;
    }

    @Override
    public void configure(Subparser subparser) {
        subparser.addArgument("-n", "--num-requests")
                .dest(NUM_REQUEST_KEY)
                .type(Integer.class)
                .required(true)
                .help("Total number of requests");


        subparser.addArgument("-c", "--concurrency-level")
                .dest(CONCURRENCY_LEVEL_KEY)
                .type(Integer.class)
                .required(true)
                .help("Concurrency level for load test");
    }

    @Override
    public void run(Bootstrap<?> bootstrap, Namespace namespace) {
        try {
            long startTime = DateTime.now().getMillis();
            int numRequests = namespace.getInt(NUM_REQUEST_KEY);
            int concurrenyLevel = namespace.getInt(CONCURRENCY_LEVEL_KEY);
            LOGGER.info(format("Running test to send %d requests with %d concurrency to webserver", numRequests, concurrenyLevel));
            boolean success = testConcurrentRequests(numRequests, concurrenyLevel);
            long secondsTaken = (DateTime.now().getMillis() - startTime) / 1000;
            double minutesTaken = secondsTaken / 60.0;
            LOGGER.info(format("time taken - seconds=%d or minutes=%.2f", secondsTaken, minutesTaken));
            if (!success) {
                throw new RuntimeException("unexpected non 200 response code");
            }
        } catch (Exception e) {
            LOGGER.error("exception while running load test on proxy.. exiting");
        }
    }
}
