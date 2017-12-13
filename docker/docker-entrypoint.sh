#!/bin/sh
set -e

if [ "$1" = 'loadmanager' -a "$(id -u)" = '0' ]; then
    chown -R mainflux:mainflux .
    dockerize -wait tcp://$DB_HOST:$DB_PORT -timeout 30s
    exec su-exec mainflux "$0" "$@"
fi

exec "$@"
