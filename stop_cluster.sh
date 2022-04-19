#!/bin/sh

kill `ps aux | grep 'server.database' | grep -v grep | awk '{print $2}'`
echo cluster stopped