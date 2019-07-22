# Proxy Redis Service

This repo is to interact with a [Redis](https://redis.io/) Datastore. Currently,
 - Webserver is based on [Dropwizard](https://www.dropwizard.io/1.0.0/docs/getting-started.html), a Java framework for developing RESTful web apps
 - Exposes a HTTP endpoint to perform read operations from Redis
 - Caches `<key,value>` pairs into a [Guava](https://github.com/google/guava/wiki) based LRU cache  
 - System offers eventual consistency, low availability in it's current form


## Setting up Proxy Service
A runtime setup requires - `docker`, `docker-compose`, `bash`

1. To run service

``./scripts/run.sh``

This spins up a Redis DB and a Webserver container. If things are working as expected one should be able to access 
- `http://localhost:8080/admin/` which is admin dashboard for Webserver 
- `http://localhost:8080/admin/metrics?pretty=true` which is useful to monitor the Webserver e.g. `timers` shows metrics for each endpoint
- `http://localhost:8080/admin/threads` shows various threads Webserver is running and their state

One can access the GET endpoint as `http://localhost:8080/v1.0/proxy?key=[KEY]`, 
where KEY is a required parameter. Webserver returns,
- http code=200 and value of key if it exists in Redis DB or Cache
- http code=400 if key is not passed e.g. for GET request `http://localhost:8080/v1.0/proxy`
- http code=404 if key is not found in Redis DB or Cache


## To run tests

``./scripts/tests.sh``

First builds a docker image with mvn and code base. It then runs Maven based tests.


## Development
To build the project
   
   This is required to build a new docker image with any changes.

   ``./scripts/build.sh``
   
   TODO: Eliminate dependency on local Maven setup to build the jar
   

## Architecture
TODO: Architecture diagram


   
   