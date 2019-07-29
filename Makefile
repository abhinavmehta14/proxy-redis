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
benchmark:
	# Work in progress for detailed benchmark
	# Does not keep connection alive for multiple requests
	# Refer README for gotchas
	# Only stderr logs showing up in docker mode
	docker-compose run benchmark 
concurrency_level=32
num_requests=1000000
single_key_get_url="http://proxy:8080/v1.0/proxy?key=a1"
keep_alive="-k" # NOTE: This is to enable the HTTP KeepAlive feature, i.e., perform multiple requests within one HTTP session. Default is no KeepAlive
benchmark_ab:
	docker-compose run --rm benchmark_ab $(keep_alive) -c $(concurrency_level) -n $(num_requests) $(single_key_get_url)
all: stop test run
