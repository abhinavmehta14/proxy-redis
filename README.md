# Proxy Redis Service

This repo is to interact with a [Redis](https://redis.io/) Datastore. Currently,
 - Webserver is based on [Dropwizard](https://www.dropwizard.io/1.0.0/docs/getting-started.html), a Java framework for developing RESTful web apps
 - Exposes a HTTP GET endpoint to perform read operations from Redis
 - Exposes a HTTP POST endpoint, a backdoor to perform write operation to Redis
 - Caches `<key,value>` pairs into a [Guava](https://github.com/google/guava/wiki) based LRU cache  
 - System offers eventual consistency, low availability in it's current form

# Development
## Running Proxy Service
A runtime setup requires - `docker`, `docker-compose`, `bash`, `make`.

Once you have such a setup run,
```shell
git clone git@github.com:abhinavmehta14/proxy-redis.git
cd proxy-redis
```

### To run / stop service
```shell
make run
```
This stops any running containers and spins up a Redis DB and a Webserver container. To access Webserver logs,
```shell
docker-compose exec proxy-redis /bin/bash
tail -f logs/*
```

Logs are appended in two separate files,
- `logs/proxy-redis-request.log` contains request logs
- `logs/proxy-redis.log` contains application logs

To stop and remove all running containers,
```shell
make stop
```

To watch Redis metrics or stats,
```shell
TODO
```

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
This is a backdoor to add key value pairs to Redis. Sample request
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
Inline with requirements, key value pairs can be added to Redis at by appending them in `RedisAppResourceIntegrationTest.initRedisStore` followed by `make test`. These pairs are persisted in DB until `docker-compose rm` or `docker-compose rm redis` is executed.

### Run Tests
```shell
make test
```
First builds a docker image with mvn and code base. It then runs Maven based tests.


### Build
This is required to build code a new image out of the code in the directory.
```shell
make build
```
A new build triggered only if all tests pass.


## TODOs
### Must
- Allocate memory / cpu for Docker containers
- Integration tests to send concurrent requests and assert response http code. Imitate `scripts/curl_test_concurrent.sh` for this

### Future work
- Log cache hit / miss metrics periodically
- Sharding (Run N redis containers and shard based on key range)
- Run a Replica Redis container for failover to ensure High Availability
- Cleanup POST endpoint which is a backdoor (since it is not a part of requirements) to add key value pairs to Redis

## Architecture
TODO: Architecture diagram
