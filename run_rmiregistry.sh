#!/bin/sh

TARGET="target/classes"

cd "$TARGET"
RMI_REGISTRY=$(pgrep rmiregistry)
if [ "$RMI_REGISTRY" ]; then
  kill "$RMI_REGISTRY"
  echo kill "$RMI_REGISTRY"
  sleep .5
fi
rmiregistry &
RMI_REGISTRY=$!
echo start rmiregistry "$RMI_REGISTRY"