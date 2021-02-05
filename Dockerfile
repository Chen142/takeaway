FROM maven:3.6.3-jdk-11

ENV environment port
ENV cluster opposite_player

COPY . /usr/src/game
WORKDIR /usr/src/game
RUN mvn clean package
CMD sh run_core.sh ${port} ${opposite_player}

