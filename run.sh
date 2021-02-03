#!/usr/bin/env bash
# how to use: run.sh playername local_port opposite_player_url (rebuild)[optional]
# sample
# bash run.sh player1 8080 player2:9090
# bash run.sh player2 9090 player1:8080

REBUILD=0
if [ $4 -eq "rebuild" ]; then
  REBUILD=1
fi

docker network create --driver bridge shared_bridge
docker container rm $1


docker build . -t game
docker run --name $1 -p $2:$2 -e port=$2 -e opposite_player=$3 -e rebuild=${REBUILD} --network shared_bridge game