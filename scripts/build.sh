#!/bin/bash
set -e
set -x
mvn package -DskipTests=true # tests are setup to run from docker image
docker build -t proxy-redis:1.0 .
