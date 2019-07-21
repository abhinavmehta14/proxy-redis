#!/bin/bash
set -e
set -x
docker-compose build proxy-test
docker-compose run proxy-test
