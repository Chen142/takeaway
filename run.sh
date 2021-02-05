#!/usr/bin/env bash
# how to use: run.sh playername local_port opposite_player_url (rebuild)[optional]
# sample
# bash run.sh player1 8080 http://player2:9090
# bash run.sh player2 9090 http://player1:8080

#stop all containers..
docker stop $1
docker container rm $1

docker network create --driver bridge shared_bridge


docker run --name $1 -p $2:$2 -e port=$2 -e opposite_player=$3 --network shared_bridge game_chen