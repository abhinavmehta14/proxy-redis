# Proxy Redis Service

This repo is to interact with a [Redis](https://redis.io/) Datastore. Currently,
 - Webserver is based on [Dropwizard](https://www.dropwizard.io/1.0.0/docs/getting-started.html), a Java framework for developing RESTful web apps
 - Exposes a HTTP endpoint to perform read operations from Redis
 - Caches `<key,value>` pairs into a [Guava](https://github.com/google/guava/wiki) based LRU cache  
 - System offers eventual consistency, low availability in it's current form

# Development
## Running Proxy Service
A runtime setup requires - `docker`, `docker-compose`, `bash`, `make`.

Once you have such a host,
```shell
git clonegit@github.com:abhinavmehta14/proxy-redis.git)
cd proxy-redis
```

### To run service

```shell
make run
```

This spins up a Redis DB and a Webserver container. If things are working as expected one should be able to access,
- <http://localhost:8080/admin/> which is admin dashboard for Webserver 
- <http://localhost:8080/admin/metrics?pretty=true> which is useful to monitor the Webserver e.g. `timers` shows metrics for each endpoint
- <http://localhost:8080/admin/threads> shows various threads Webserver is running and their state

One can access the GET endpoint as `http://localhost:8080/v1.0/proxy?key=[KEY]`, where KEY is a required parameter. Webserver returns,
- http code=200 and value of key if it exists in Redis DB or Cache
- http code=400 if key is not passed e.g. for GET request <http://localhost:8080/v1.0/proxy>
- http code=404 if key is not found in Redis DB or Cache


### To run tests

```shell
make test
```

First builds a docker image with mvn and code base. It then runs Maven based tests.


### To build a code change
This is required to build a new docker image with any changes.
```shell
make build
```


## Architecture
TODO: Architecture diagram


   
   