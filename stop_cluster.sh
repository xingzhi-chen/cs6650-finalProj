#!/bin/sh

kill `ps aux | grep 'server.route' | grep -v grep | awk '{print $2}'`
kill `ps aux | grep 'server.room' | grep -v grep | awk '{print $2}'`
kill `ps aux | grep 'server.login' | grep -v grep | awk '{print $2}'`
kill `ps aux | grep 'server.database' | grep -v grep | awk '{print $2}'`
echo cluster stopped