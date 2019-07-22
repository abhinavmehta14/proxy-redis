#!/bin/bash
set -e
set -x
mvn package -DskipTests=true # currently tests are setup to run from docker image only
docker build -t proxy-redis:1.0 .
