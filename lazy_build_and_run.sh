#!/usr/bin/env bash
#Combine builder and run together in case the test machine doesn't have java11. Can be separated into 2 images if we do it seriously.
PORT=$1
OPPOSITE_URL=$2
if [ 1 -eq $3 ]; then
  mvn clean package
fi

java -jar target/Game_Of_3_Takeaway_com-1.0-SNAPSHOT.jar -xmx=1g -xms=1g --server.port="$PORT" --game.opposite.url="$OPPOSITE_URL"