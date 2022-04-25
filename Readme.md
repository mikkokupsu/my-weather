# MyWeather

MyWeather is a fictitious website that provides air temperature data to the customers who purchased a subscription for a specific location (or coordinate).

# Project structure

Project is composed of backend and frontend.

See section *API specification* for more details about the backend API.

## Backend

Project developed with Java and dependencies are handled with Gradle.

Development server requires Amazon DynamoDB Local to be running if you want try out the API.

**To run Amazon DynamoDB Local**
```
docker run -it -p 8000:8000 amazon/dynamodb-local
```

**To run development server**
```
AWS_ACCESS_KEY_ID='a' AWS_SECRET_ACCESS_KEY='b' AWS_DYNAMODB_LOCAL_ADDRESS='http://localhost:8000' ./gradlew bootRun
```

**To run unit tests**
```
./gradlew test
```

## Frontend

Project developed with React and dependencies are handled with npm.

**To run developement server**
```
npm start
```

## Docker compose

**Start service in Docker containers**
```
docker-compose up
```

**Start service in Docker containers with rebuilding the containers**
```
docker-compose up --build
```

## API specification

API specification can be found [here](api/myweather.com.yml)

You can use [Swagger Editor](https://editor.swagger.io/?url=https://raw.githubusercontent.com/mikkokupsu/my-weather/main/api/myweather.com.yml) to view and/or edit the file.

## Environment and version information

**npm**
```
$ npm --version
8.1.2
```

**java**
```
$ java --version
openjdk 11.0.11 2021-04-20
OpenJDK Runtime Environment (build 11.0.11+9-Ubuntu-0ubuntu2.20.04)
OpenJDK 64-Bit Server VM (build 11.0.11+9-Ubuntu-0ubuntu2.20.04, mixed mode, sharing)
```

**Gradle**
```
$ ./gradlew --version

------------------------------------------------------------
Gradle 7.4.1
------------------------------------------------------------

Build time:   2022-03-09 15:04:47 UTC
Revision:     36dc52588e09b4b72f2010bc07599e0ee0434e2e

Kotlin:       1.5.31
Groovy:       3.0.9
Ant:          Apache Ant(TM) version 1.10.11 compiled on July 10 2021
JVM:          11.0.11 (Ubuntu 11.0.11+9-Ubuntu-0ubuntu2.20.04)
OS:           Linux 4.4.0-19041-Microsoft amd64
```

**docker**
```
$ docker --version
Docker version 20.10.10, build b485636
```

***docker-compose***
```
$ docker-compose --version
docker-compose version 1.25.0, build unknown
```