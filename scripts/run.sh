#!/bin/bash
set -e
set -x
docker-compose up -d redis proxy-redis

## alternatively use docker command below (helps debug dying container)
# docker run --name redis-container -p 7001:6379 -d redis
# docker run --name proxy-redis  -p 8080:8080 --rm -it proxy-redis:1.0 java -jar proxy-redis-1.0-SNAPSHOT.jar server redis-app.yml

## alternatively to run build locally
# java -jar target/proxy-redis-1.0-SNAPSHOT.jar server src/main/resources/redis-app.yml
