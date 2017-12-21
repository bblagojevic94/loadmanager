#!/bin/bash

set -ex

VERSION=$(grep 'version := ' build.sbt | sed 's:^[^"]*"\([^"]*\)".*:\1:')

# prepare build artifacts
sbt ++$TRAVIS_SCALA_VERSION clean dist
unzip target/universal/loadmanager-*.zip -d docker
mv docker/loadmanager-* docker/loadmanager

# build an image and authenticate to docker hub
docker build --rm -t image docker
docker login -u "$DOCKER_USERNAME" -p "$DOCKER_PASSWORD"

# tag & push artifacts
docker tag image mainflux/load-manager:latest
docker push mainflux/load-manager:latest
docker tag image mainflux/load-manager:$VERSION
docker push mainflux/load-manager:$VERSION
