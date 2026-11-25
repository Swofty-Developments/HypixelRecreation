#!/bin/bash

if [ -n "$FORWARDING_SECRET" ]; then
    secret="$FORWARDING_SECRET"
elif [ -f ./configuration_files/forwarding.secret ]; then
    secret=$(cat ./configuration_files/forwarding.secret)
else
    echo "FORWARDING_SECRET is required (env var or configuration_files/forwarding.secret)" >&2
    exit 1
fi

printf '%s' "$secret" > ./forwarding.secret

mkdir -p ./configuration
cp -an ./configuration_files/. ./configuration/ 2>/dev/null || true

if ! ls ./configuration/world/*.polar >/dev/null 2>&1; then
    echo "ERROR: no world files found in ./configuration/world." >&2
    echo "Download the world files (see https://opensource.swofty.net/docs/setup/game-servers)," >&2
    echo "extract them into the configuration folder of the repository and restart this container." >&2
    exit 1
fi

# Ensure configuration/config.yml exists
if [ ! -f ./configuration/config.yml ]; then
    if [ -f configuration_files/config.docker.yml ]; then
        cp configuration_files/config.docker.yml ./configuration/config.yml
    else
        cp configuration_files/config.example.yml ./configuration/config.yml
    fi
fi

# Update config.yml with the forwarding secret (velocity-secret)
sed -i "s/velocity-secret: .*/velocity-secret: '$secret'/" ./configuration/config.yml

echo "$SERVICE_CMD"
exec $SERVICE_CMD