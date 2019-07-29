# Proxy Redis Service

This service is to interact with a [Redis](https://redis.io/) Datastore via HTTP. Setup is meant to be scalable and handle concurrent requests. Service uses the below building blocks,
 - Webserver is based on [Dropwizard](https://www.dropwizard.io/1.0.0/docs/getting-started.html), a Java framework for developing RESTful web apps
 - Exposes a HTTP GET endpoint to perform read operations from Redis
 - Exposes a HTTP POST endpoint, a backdoor to perform write operation to Redis
 - Caches `<key,value>` pairs into a [Guava](https://github.com/google/guava/wiki) based LRU cache

System in it's current form offers,
1. Eventual Consistency
2. Not Highly Available (HA)

HA can be offered with current setup with minor changes.

## Development - Running Proxy Service
### Prerequisites
- A runtime setup requires - `docker`, `docker-compose`, `bash`, `make`
- Execute `docker login` for ability to pull public images. Clone repo with commands below
- Webserver runs on port 8080 and Redis is exposed on 7001. Make sure these two ports are available or change ports in `Dockerfile.build` and `resources/redis-app.yml`
- Clone repo and change directory
```shell
git clone git@github.com:abhinavmehta14/proxy-redis.git
cd proxy-redis
```

### Run Tests
```shell
make test
```
This executes the below steps in they are mentioned,
1. Builds a docker image with mvn and code base
2. Runs Maven based tests, loads Redis store with key value pairs specified in `RedisAppResourceIntegrationTest.initRedisStore`
3. A new build is triggered only if all tests pass

### Run Service
Below command stops existing proxy container and spins up a new Webserver container
```shell
make run
```
Note that, above command requires at least one execution of `make test` before `make run` to generate image for Webserver. The Webserver is not yet pulled from Docker cloud yet.

To access Webserver logs,
```shell
docker-compose exec proxy-redis /bin/bash
tail -f logs/*
```

Logs are appended in two separate files,
- `logs/proxy-redis-request.log` contains request logs
- `logs/proxy-redis.log` contains application logs

To watch Redis metrics or stats,
```shell
> redis-cli -h localhost -p 7001
localhost:7001> info
# Server
redis_version:5.0.5
...
# Stats
total_connections_received:7
total_commands_processed:22
instantaneous_ops_per_sec:0
total_net_input_bytes:663
total_net_output_bytes:14857
instantaneous_input_kbps:0.00
instantaneous_output_kbps:0.00
rejected_connections:0
sync_full:0
sync_partial_ok:0
sync_partial_err:0
expired_keys:0
expired_stale_perc:0.00
expired_time_cap_reached_count:0
evicted_keys:0
keyspace_hits:1
keyspace_misses:1
pubsub_channels:0
pubsub_patterns:0
latest_fork_usec:0
migrate_cached_sockets:0
slave_expires_tracked_keys:0
active_defrag_hits:0
active_defrag_misses:0
active_defrag_key_hits:0
active_defrag_key_misses:0
```

Note that the above method assumes you have `redis-cli` setup which is not a part of system requirements

#### Admin and Metrics Endpoint
- <http://localhost:8080/admin/> is admin dashboard for Webserver
- <http://localhost:8080/admin/metrics?pretty=true> is useful to monitor the Webserver e.g. `timers` shows useful metrics like `min`, `max`, `t99`, `stddev` etc for each endpoint
- <http://localhost:8080/admin/threads> shows various threads Webserver is running and their state

#### GET Endpoint
One can access the GET endpoint as <http://localhost:8080/v1.0/proxy?key=[KEY]>, where KEY is a required parameter. Webserver returns,
- http code=200 and value of key if it exists in Redis DB or Cache
- http code=400 if key is not passed e.g. for GET request <http://localhost:8080/v1.0/proxy>
- http code=404 if key is not found in Redis DB or Cache

#### POST Endpoint
This is a backdoor (not part of requirements) to add key value pairs to Redis. Sample request
```shell
curl -i -XPOST 'http://localhost:8080/v1.0/proxy?key=a5&value=b5'; echo
```
Returns,
- http code=200 if pair was added or updated successfully
- http code=400 if either key or value parameters are not passed
- http code=500 if something went wrong while performing update and DB state for this key value is unknown

Alternatively, one can install and use `redis-cli` to access Redis DB for writes (or reads) as follows,
```shell
redis-cli -h localhost -p 7001
localhost:7001> set a1 b1
OK
localhost:7001> get a1
"b1"
```

#### Adding Key, Value Pairs to Redis
Inline with requirements, key value pairs can be added to Redis at by appending them in `RedisAppResourceIntegrationTest.initRedisStore` followed by `make test` which ensures pairs are set in Redis. These pairs are persisted in DB until `docker-compose rm` or `docker-compose rm redis` is executed.


### Stop Service
To stop and remove all running containers including data on Redis,
```shell
 > make stop
docker-compose stop
Stopping proxy    ... done
Stopping redis-db ... done
docker-compose rm
Going to remove proxy, proxy-redis_redis_run_1, proxy-redis_proxy-test_run_4, proxy-redis_proxy-test_run_3, proxy-redis_proxy-test_run_2, proxy-redis_proxy-test_run_1, redis-db
Are you sure? [yN] y
Removing proxy                        ... done
Removing proxy-redis_redis_run_1      ... done
Removing proxy-redis_proxy-test_run_4 ... done
Removing proxy-redis_proxy-test_run_3 ... done
Removing proxy-redis_proxy-test_run_2 ... done
Removing proxy-redis_proxy-test_run_1 ... done
Removing redis-db                     ... done
```

To stop Webserver container only,
```shell
docker-compose stop proxy-redis
```

### Run Everything
To bring up Redis store, run tests, build webserver image, start Webserver one can invoke the below command
```shell
make all
```
This combines all the make tasks.

### Test sending concurrent request
[Work in progress]
Class `com.amehta.proxy.redis.interact.test.RedisAppConcurrentRequestsTest` can be used send concurrent request to HTTP Webserver. The number of requests can be configured in `main()` method. 

TODO: Figure out how to run a standalone from dropwizard based fat jar and use the command to dockerize this test.

Currently, I can run this class in a native dev environment and see that concurrent requests are being handled. 


## Architecture
TODO: Architecture Diagram
1. GET endpoint

     Endpoint is served by a multi-threaded webserver where each thread handles requests from a bounded queue of pending requests. Request handler first checks Guava Cache for presence of key
     - If found, return value
     - If not found, get key value from Redis. Add it to Cache return

    Cache has various tunable parameters - TTL, cache size in terms of number of entries
    Redis keys are available until a configurable global expiry time.


2. POST endpoint

     TODO

## Algorithmic Complexity
Guava Cache or Redis lookup or insert is O(1) for all practical purposes.

In practice, the lookup in each _segment_ of _ConcurrentHashMap_ is non-constant (a TODO: `log` or `linear` function of size of segment).

Also, multiple threads writing to same _segment_ can cause contention issues. Hence, complexity is also a function of number of threads writing to Cache.

TODO: Elaborate on details above.

## Future Work
### Must
- Allocate memory limit for Redis and cpu limits for Redis and Proxy containers
- ~~For graceful shutdown, `CachedRedisService` should implement `io.dropwizard.lifecycle.Managed` interface~~
- Address problems with concurrency test class `ProxyLoadTestCommand` to send concurrent requests and assert response http code. ~~Dockerize and run from fat jar~~
- ~~Another bash based attempt `scripts/curl_test_concurrent.sh` needs to be Dockerized~~ Not Applicable
- Read `globalExpiry` from config in `RedisAppResourceIntegrationTest`
- ~~Test to validate config. Such errors are showing up on runtime currently~~ Command `java -jar FAT_JAR` addresses this 
- Impose timeout on DB Read / Write calls
- HTTP GET support for non-string value [data types](https://redis.io/topics/data-types)

### Good to have
- ~~Log cache hit / miss metrics periodically~~
- Sharding (Run N redis containers and shard based on key range)
- Run a Replica Redis container for failover to ensure High Availability
- Cleanup POST endpoint which is a backdoor (since it is not a part of requirements) to add key value pairs to Redis


## Benchmark
Benchmarks below are done using [Apache HTTP server benchmarking tool](https://httpd.apache.org/docs/2.4/programs/ab.html) for GET endpoint hosted by single replica of Proxy Service. Command to run benchmark,
```shell
make benchmark_ab
```

Various parameters (`concurrency_level`, `concurrency_level`, `keep_alive`) are controlled from within the `Makefile`

Results,

| Special conditions | #Keys queried | #Requests | Concurrency | mean | t50 | t75 | t95 | t99 | t100 | Non-2xx responses | Requests per second | response body size (bytes) |
| --- | ------------- | --------- | ----------- | ---- | --- | --- | --- | --- | ---- | --- | --- | --- |
| no connection keep alive | 1 | 100,000 | 10 | 34 | 26 | 32 | 46 | 208 | 1259 | - | - | 13
| no connection keep alive | 1 | 100,000 | 32 | 113 | 87 | 107 | 275 | 540 | 4853 | - | - | 13
| **with connection keep alive** | 1 | 1000,000 | 32 | 3 | 0 | 1 | 14| 65 | 400 | 100 |  640 | 13 

Note that, `t{xx}` is in milliseconds

TODO: Improve benchmarking capabilities for multiple url using JMeter (`docker pull justb4/jmeter`)


## Benchmark (work in progress)
This benchmark uses `ProxyLoadTestCommand` class, a command line utility for advanced assertions on load test like response code, body
TODOs:
 1. setup stdout logs from benchmark not appearing on console
 2. Investigate `NoRouteToHost` exception

Benchmark can be invoked with the command below,
```
make benchmark
```

To connect to running benchmark container
```shell
docker-compose ps # To get service name for benchmark e.g. proxy-redis_benchmark_run_3
docker exec -it proxy-redis_benchmark_run_[%s] bash # use suffix from the output of the command above
```


 
## Overall Effort

| Task  | Hours | Notes |
| ----------- | :-----------: | :--- |
| Understanding Requirements | 1 |
| Setting up a Dropwizard webserver  | 1.5  |
| Code  | .5  |
| Manual testing | 1 |
| Tests + Refactoring | 1 |
| Dockerize | 3 | #docker-newbie
| Makefile | .5 | 
| Documentation | 1 |
| Re-iterating for code to match requirements | 1 |
| Concurrency test | .5 | Incomplete. Need to read up on Dropwizard on how to run a java class
