FROM anapsix/alpine-java:8_server-jre
MAINTAINER Mainflux

WORKDIR /opt

COPY ./loadmanager .
COPY ./docker-entrypoint.sh /usr/local/bin

ENV DOCKERIZE v0.6.0

RUN addgroup -S mainflux && \
    adduser -SG mainflux mainflux && \
    apk add --no-cache 'su-exec>=0.2' openssl && \
    ln -s /opt/bin/loadmanager /usr/local/bin/loadmanager && \
    wget https://github.com/jwilder/dockerize/releases/download/$DOCKERIZE/dockerize-alpine-linux-amd64-$DOCKERIZE.tar.gz && \
    tar -C /usr/local/bin -xzvf dockerize-alpine-linux-amd64-$DOCKERIZE.tar.gz && \
    rm dockerize-alpine-linux-amd64-$DOCKERIZE.tar.gz && \
    apk del openssl

EXPOSE 9000

ENTRYPOINT ["docker-entrypoint.sh"]
CMD ["loadmanager"]
