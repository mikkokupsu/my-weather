FROM gradle:7.4.1-jdk11

WORKDIR /app

COPY backend/ ./

RUN gradle build

ENTRYPOINT gradle bootRun