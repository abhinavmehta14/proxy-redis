run:
	# Maintaining a separate file since it has several useful instructions / debugging hacks
	docker-compose stop proxy-redis
	./scripts/run.sh
build: test
	docker-compose build proxy-build
test:
	docker-compose build proxy-test
	docker-compose run proxy-test
stop:
	docker-compose stop
	docker-compose rm
