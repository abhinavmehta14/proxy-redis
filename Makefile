run:
	# Maintaining a separate file since it has several useful instructions / debugging hacks
	docker-compose stop proxy-redis
	./scripts/run.sh
test:
	docker-compose build proxy-test
	docker-compose run proxy-test
	docker-compose build proxy-build
stop:
	docker-compose stop
	docker-compose rm
all: stop test run
