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

You can use [Swagger Editor](https://editor.swagger.io/) to view and/or edit the file.