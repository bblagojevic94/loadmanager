#!/bin/sh
set -e

# allow the container to be started with `--user`
if [ "$1" = 'loadmanager' -a "$(id -u)" = '0' ]; then
    chown -R loadmanager:loadmanager .
    exec su-exec loadmanager "$0" "$@"
fi

exec "$@"
