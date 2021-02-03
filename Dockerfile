FROM maven:3.6.3-jdk-11

ENV environment port
ENV cluster opposite_player
ENV rebuild 0

COPY . /usr/src/game
WORKDIR /usr/src/game
CMD sh lazy_build_and_run.sh ${port} ${opposite_player} ${rebuild}

