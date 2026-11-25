#!/bin/sh
set -e

if [ -z "$FORWARDING_SECRET" ]; then
    echo "FORWARDING_SECRET is required" >&2
    exit 1
fi

printf '%s' "$FORWARDING_SECRET" > /app/forwarding.secret

mkdir -p /app/configuration
if [ -f /app/configuration_files/config.yml ]; then
    cp /app/configuration_files/config.yml /app/configuration/config.yml
fi
if [ -f /app/configuration_files/velocity.toml ]; then
    cp /app/configuration_files/velocity.toml /app/velocity.toml
fi

sed -i "s/velocity-secret: .*/velocity-secret: '$FORWARDING_SECRET'/" /app/configuration/config.yml

exec java -jar velocity.jar
