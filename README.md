# Proxy Redis Service

This repo is to interact with a [Redis](https://redis.io/) Datastore. Currently,
 - Webserver is based on [Dropwizard](https://www.dropwizard.io/1.0.0/docs/getting-started.html), a Java framework for developing RESTful web apps
 - Exposes a HTTP GET endpoint to perform read operations from Redis
 - Exposes a HTTP POST endpoint, a backdoor to perform write operation to Redis
 - Caches `<key,value>` pairs into a [Guava](https://github.com/google/guava/wiki) based LRU cache
 - System offers eventual consistency, low availability in it's current form

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
 amehta@localhost 16:12:06:~ > redis-cli -h localhost -p 7001
localhost:7001> info
# Server
redis_version:5.0.5
redis_git_sha1:00000000
redis_git_dirty:0
redis_build_id:f5cc35eb8e511133
redis_mode:standalone
os:Linux 4.9.93-linuxkit-aufs x86_64
arch_bits:64
multiplexing_api:epoll
atomicvar_api:atomic-builtin
gcc_version:8.3.0
process_id:1
run_id:b91ff0f2fcdb172b6e0f22409b388344f74edcf2
tcp_port:6379
uptime_in_seconds:471
uptime_in_days:0
hz:10
configured_hz:10
lru_clock:3556042
executable:/data/redis-server
config_file:

# Clients
connected_clients:1
client_recent_max_input_buffer:2
client_recent_max_output_buffer:0
blocked_clients:0

# Memory
used_memory:854480
used_memory_human:834.45K
used_memory_rss:5484544
used_memory_rss_human:5.23M
used_memory_peak:915312
used_memory_peak_human:893.86K
used_memory_peak_perc:93.35%
used_memory_overhead:841198
used_memory_startup:791240
used_memory_dataset:13282
used_memory_dataset_perc:21.00%
allocator_allocated:1423640
allocator_active:1708032
allocator_resident:10895360
total_system_memory:12575707136
total_system_memory_human:11.71G
used_memory_lua:37888
used_memory_lua_human:37.00K
used_memory_scripts:0
used_memory_scripts_human:0B
number_of_cached_scripts:0
maxmemory:0
maxmemory_human:0B
maxmemory_policy:noeviction
allocator_frag_ratio:1.20
allocator_frag_bytes:284392
allocator_rss_ratio:6.38
allocator_rss_bytes:9187328
rss_overhead_ratio:0.50
rss_overhead_bytes:-5410816
mem_fragmentation_ratio:6.75
mem_fragmentation_bytes:4672064
mem_not_counted_for_evict:0
mem_replication_backlog:0
mem_clients_slaves:0
mem_clients_normal:49694
mem_aof_buffer:0
mem_allocator:jemalloc-5.1.0
active_defrag_running:0
lazyfree_pending_objects:0

# Persistence
loading:0
rdb_changes_since_last_save:18
rdb_bgsave_in_progress:0
rdb_last_save_time:1563836659
rdb_last_bgsave_status:ok
rdb_last_bgsave_time_sec:-1
rdb_current_bgsave_time_sec:-1
rdb_last_cow_size:0
aof_enabled:0
aof_rewrite_in_progress:0
aof_rewrite_scheduled:0
aof_last_rewrite_time_sec:-1
aof_current_rewrite_time_sec:-1
aof_last_bgrewrite_status:ok
aof_last_write_status:ok
aof_last_cow_size:0

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

# Replication
role:master
connected_slaves:0
master_replid:0109335829ce22c0f27f90bd26ae0a69dad3cb35
master_replid2:0000000000000000000000000000000000000000
master_repl_offset:0
second_repl_offset:-1
repl_backlog_active:0
repl_backlog_size:1048576
repl_backlog_first_byte_offset:0
repl_backlog_histlen:0

# CPU
used_cpu_sys:1.680000
used_cpu_user:0.380000
used_cpu_sys_children:0.000000
used_cpu_user_children:0.000000

# Cluster
cluster_enabled:0

# Keyspace
db0:keys=5,expires=0,avg_ttl=0
```

Note that the above method assumes you have redis-cli setup

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
- Allocate memory / cpu for Docker containers
- Integration tests to send concurrent requests and assert response http code. Imitate `scripts/curl_test_concurrent.sh` for this
- Read `globalExpiry` from config in `RedisAppResourceIntegrationTest`
- High Consistency when we are running only single redis container
- Test to validate config. Such errors are showing up on runtime currently
- Impose timeout on DB Read / Write calls

### Good to have
- Log cache hit / miss metrics periodically
- Sharding (Run N redis containers and shard based on key range)
- Run a Replica Redis container for failover to ensure High Availability
- Cleanup POST endpoint which is a backdoor (since it is not a part of requirements) to add key value pairs to Redis

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
|Re-iterating for code to match requirements | 1 |
