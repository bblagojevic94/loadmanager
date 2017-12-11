FROM anapsix/alpine-java:8_server-jre
MAINTAINER Bojan Blagojević <blagojevicb94@gmail.com>

WORKDIR /opt

COPY ./loadmanager .
COPY ./docker-entrypoint.sh /usr/local/bin

RUN addgroup -S loadmanager \
    && adduser -SG loadmanager loadmanager \
    && apk add --no-cache 'su-exec>=0.2' \
    && ln -s /opt/bin/loadmanager /usr/local/bin/loadmanager

EXPOSE 9000

ENTRYPOINT ["docker-entrypoint.sh"]
CMD ["loadmanager"]
