package com.myweather.backend.database;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.dynamodbv2.model.DeleteItemRequest;
import com.amazonaws.services.dynamodbv2.model.DeleteItemResult;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.amazonaws.services.dynamodbv2.model.QueryResult;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import com.amazonaws.services.dynamodbv2.model.UpdateItemRequest;
import com.amazonaws.services.dynamodbv2.model.UpdateItemResult;
import com.myweather.exception.AwsError;
import com.myweather.exception.NotFound;
import com.myweather.backend.database.model.AirTemperature;
import com.myweather.backend.util.DynamoDbUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class AirTemperatureRepoitory implements IAirTemperatureRepository {

    private static final Logger logger = LogManager.getLogger(AirTemperatureRepoitory.class);

    private static final String TABLE_NAME = "MyWeather";

    @Autowired
    private AmazonDynamoDB client;

    public AirTemperatureRepoitory(final AmazonDynamoDB client) {
        this.client = client;
    }

    /***
     * {@inheritDoc}
     */
    @Override
    public List<AirTemperature> get(String location, LocalDateTime start, LocalDateTime stop) throws AwsError {
        QueryRequest queryRequest = new QueryRequest()
            .withTableName(TABLE_NAME)
            .withKeyConditionExpression("#pk = :pk AND #sk BETWEEN :start AND :stop")
            .withExpressionAttributeNames(Map.of("#pk", "Pk", "#sk", "Sk"))
            .withExpressionAttributeValues(Map.of(
                ":pk", new AttributeValue(location),
                ":start", new AttributeValue(start.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)),
                ":stop", new AttributeValue(stop.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))));
        QueryResult result;
        try {
            result = client.query(queryRequest);
        } catch (Exception ex) {
            String msg = String.format(
                "Failed to get location=%s between %s and %s",
                location, start.toString(), stop.toString());
            logger.error(msg, ex);
            throw new AwsError(msg);
        }

        logger.info(() -> String.format(
            "Queried location=%s start=%s stop=%s with status=%d",
            location,
            start.toString(),
            stop.toString(),
            result.getSdkHttpMetadata().getHttpStatusCode()));

        return result.getItems().stream().map(DynamoDbUtil::fromDynamoDbItem).collect(Collectors.toList());
    }

    /***
     * {@inheritDoc}
     */
    @Override
    public AirTemperature save(AirTemperature airTemperature) throws AwsError {
        Map<String, AttributeValue> item = DynamoDbUtil.toDynamoDbItem(airTemperature);
        PutItemRequest putItemRequest = new PutItemRequest()
            .withTableName(TABLE_NAME)
            .withItem(item);
        PutItemResult result;
        try {
            result = client.putItem(putItemRequest);
        } catch (Exception exception) {
            String msg = String.format(
                "Failed to save location=%s timestamp=%s value=%f",
                airTemperature.location,
                airTemperature.timestamp.toString(),
                airTemperature.value);
            logger.error(msg, exception);
            throw new AwsError(msg);
        }

        logger.info(() -> String.format(
            "Saved location=%s timestamp=%s value=%f with status=%d",
            airTemperature.location,
            airTemperature.timestamp.toString(),
            airTemperature.value,
            result.getSdkHttpMetadata().getHttpStatusCode()));

        return airTemperature;
    }

    /***
     * {@inheritDoc}
     */
    @Override
    public AirTemperature update(AirTemperature airTemperature) throws AwsError, NotFound {
        Map<String, AttributeValue> item = DynamoDbUtil.toDynamoDbItem(airTemperature, true);
        UpdateItemRequest updateItemRequest = new UpdateItemRequest()
            .withTableName(TABLE_NAME)
            .withKey(item)
            .withConditionExpression("attribute_exists(#pk)")
            .withUpdateExpression("SET #value = :newvalue")
            .withExpressionAttributeNames(Map.of("#value", "value", "#pk", "Pk"))
            .withExpressionAttributeValues(Map.of(":newvalue", new AttributeValue().withN(String.valueOf(airTemperature.value))));

        UpdateItemResult result;
        try {
            result = client.updateItem(updateItemRequest);
        } catch (ConditionalCheckFailedException ex) {
            String msg = String.format("Could not find to update location=%s timestamp=%s", 
                airTemperature.location, airTemperature.timestamp.toString());
            logger.error(msg, ex);
            throw new NotFound(msg);
        } catch (Exception ex) {
            String msg = String.format("Failed to update location=%s timestamp=%s value=%f", 
                airTemperature.location, airTemperature.timestamp.toString(), airTemperature.value);
            logger.error(msg, ex);
            throw new AwsError(msg);
        }

        logger.info(() -> String.format(
            "Updated location=%s timestamp=%s value=%f with status=%d",
            airTemperature.location,
            airTemperature.timestamp.toString(),
            airTemperature.value,
            result.getSdkHttpMetadata().getHttpStatusCode()));

        return airTemperature;
    }

    /***
     * {@inheritDoc}
     */
    @Override
    public AirTemperature delete(AirTemperature airTemperature) throws AwsError, NotFound {
        DeleteItemRequest deleteItemRequest = new DeleteItemRequest()
            .withTableName(TABLE_NAME)
            .withKey(DynamoDbUtil.toDynamoDbItem(airTemperature, true))
            .withConditionExpression("attribute_exists(#pk)")
            .withExpressionAttributeNames(Map.of("#pk", "Pk"))
            .withReturnValues(ReturnValue.ALL_OLD);

        DeleteItemResult result;
        try {
            result = client.deleteItem(deleteItemRequest);
        } catch (ConditionalCheckFailedException ex) {
            String msg = String.format("Could not find to delete location=%s timestamp=%s", 
                airTemperature.location, airTemperature.timestamp.toString());
            logger.error(msg, ex);
            throw new NotFound(msg);
        } catch (Exception ex) {
            String msg = String.format("Failed to delete location=%s timestamp=%s",
                airTemperature.location, airTemperature.timestamp.toString());
            logger.error(msg, ex);
            throw new AwsError(msg);
        }

        logger.info(() -> String.format(
            "Deleted location=%s timestamp=%s with status=%d",
            airTemperature.location,
            airTemperature.timestamp.toString(),
            result.getSdkHttpMetadata().getHttpStatusCode()));

        return DynamoDbUtil.fromDynamoDbItem(result.getAttributes());
    }
}
