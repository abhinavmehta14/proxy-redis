FROM openjdk:8-jre-alpine                                 

# To resolve "UnsatisfiedLinkError: /tmp/snappy*-libsnappyjava.so:" error
RUN apk update && apk add --no-cache libc6-compat
RUN ln -s /lib/libc.musl-x86_64.so.1 /lib/ld-linux-x86-64.so.2

# add bash to run wrapper shell scripts
RUN apk add bash

# expose port
EXPOSE 8080/tcp

# work dir
WORKDIR /code/proxy-redis

# add jar and config
ADD target/proxy-redis-1.0-SNAPSHOT.jar . 
ADD src/main/resources/redis-app.yml . 
RUN mkdir logs
