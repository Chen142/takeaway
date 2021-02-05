#!/usr/bin/env bash
PORT=$1
OPPOSITE_URL=$2

java -jar target/Game_Of_3_Takeaway_com-1.0-SNAPSHOT.jar -xmx=1g -xms=1g --server.port="$PORT" --game.opposite.url="$OPPOSITE_URL"