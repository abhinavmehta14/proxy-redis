#!/bin/bash
set -e
set -x
mvn package
docker build -t proxy-redis:1.0 .
