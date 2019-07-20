#!/bin/bash
set -e
set -x
# TODO: Figure out stop by container name
docker-compose stop
docker-compose rm

