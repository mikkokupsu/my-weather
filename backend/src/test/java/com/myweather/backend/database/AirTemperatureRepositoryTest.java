package com.myweather.backend.database;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.amazonaws.http.HttpResponse;
import com.amazonaws.http.SdkHttpMetadata;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.dynamodbv2.model.DeleteItemRequest;
import com.amazonaws.services.dynamodbv2.model.DeleteItemResult;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.amazonaws.services.dynamodbv2.model.QueryResult;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;
import com.amazonaws.services.dynamodbv2.model.UpdateItemRequest;
import com.amazonaws.services.dynamodbv2.model.UpdateItemResult;

import com.myweather.exception.AwsError;
import com.myweather.exception.NotFound;
import com.myweather.backend.database.model.AirTemperature;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class AirTemperatureRepositoryTest {

    
    private final String location = "test location";
    private final LocalDateTime start = LocalDateTime.of(2022, 4, 24, 10, 0, 0);
    private final LocalDateTime stop = LocalDateTime.of(2022, 4, 24, 12, 0, 0);
    private final LocalDateTime timestamp = LocalDateTime.of(2022, 4, 24, 11, 0, 0);
    private final double value = 1.23d;

    private final AirTemperature airTemperature = new AirTemperature(timestamp, value, location);

    private AmazonDynamoDB client;
    
    private AirTemperatureRepoitory repository;

    private HttpResponse response;

    @BeforeEach
    public void setUp() {
        client = Mockito.mock(AmazonDynamoDB.class);
        repository = new AirTemperatureRepoitory(client);
        response = new HttpResponse(null, null);
        response.setStatusCode(200);
    }
    
    @Test
    public void testGetHappy() throws Exception {
        
        String timestamp1 = "2022-04-24T10:00:00";
        String value1 = "1.10";
        String timestamp2 = "2022-04-24T11:00:00";
        String value2 = "1.11";
        String timestamp3 = "2022-04-24T12:00:00";
        String value3 = "1.12";

        List<Map<String, AttributeValue>> items = new LinkedList<>();
        items.add(Map.of("Pk", new AttributeValue(location), "Sk", new AttributeValue(timestamp1), "value", new AttributeValue().withN(value1)));
        items.add(Map.of("Pk", new AttributeValue(location), "Sk", new AttributeValue(timestamp2), "value", new AttributeValue().withN(value2)));
        items.add(Map.of("Pk", new AttributeValue(location), "Sk", new AttributeValue(timestamp3), "value", new AttributeValue().withN(value3)));
        QueryResult result = new QueryResult().withItems(items); 
        result.setSdkHttpMetadata(SdkHttpMetadata.from(response));
        Mockito.when(client.query(any(QueryRequest.class))).thenReturn(result);

        List<AirTemperature> measurements = repository.get(location, start, stop);

        Assertions.assertEquals(3, measurements.size());
        
        Assertions.assertEquals(location, measurements.get(0).location);
        Assertions.assertEquals(timestamp1, measurements.get(0).timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        Assertions.assertEquals(Double.parseDouble(value1), measurements.get(0).value);

        Assertions.assertEquals(location, measurements.get(1).location);
        Assertions.assertEquals(timestamp2, measurements.get(1).timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        Assertions.assertEquals(Double.parseDouble(value2), measurements.get(1).value);

        Assertions.assertEquals(location, measurements.get(2).location);
        Assertions.assertEquals(timestamp3, measurements.get(2).timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        Assertions.assertEquals(Double.parseDouble(value3), measurements.get(2).value);

        verifyGetByDynamo();
        Mockito.verifyNoMoreInteractions(client);
    }

    @Test
    public void testGetFails() throws Exception {
        Mockito.when(client.query(any(QueryRequest.class))).thenThrow(new ResourceNotFoundException("test"));
        Assertions.assertThrows(AwsError.class, () -> repository.get(location, start, stop));
        verifyGetByDynamo();
        Mockito.verifyNoMoreInteractions(client);
    }

    private void verifyGetByDynamo() {
        Mockito.verify(client).query(argThat((QueryRequest request) -> 
            request.getTableName().equals("MyWeather") &&
                request.getKeyConditionExpression().equals("#pk = :pk AND #sk BETWEEN :start AND :stop") && 
                request.getExpressionAttributeNames().equals(Map.of("#pk", "Pk", "#sk", "Sk")) &&
                request.getExpressionAttributeValues().equals(Map.of(
                    ":pk", new AttributeValue(location),
                    ":start", new AttributeValue(start.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)),
                    ":stop", new AttributeValue(stop.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))))
        ));
    }

    @Test
    public void testSaveHappy() throws Exception {
        PutItemResult result = new PutItemResult();
        result.setSdkHttpMetadata(SdkHttpMetadata.from(response));
        Mockito.when(client.putItem(any(PutItemRequest.class))).thenReturn(result);

        AirTemperature saved = repository.save(airTemperature);

        Assertions.assertEquals(location, saved.location);
        Assertions.assertEquals(timestamp, saved.timestamp);
        Assertions.assertEquals(value, saved.value);

        verifySaveByDynamo();        
        Mockito.verifyNoMoreInteractions(client);
    }

    @Test
    public void testSaveFails() {
        Mockito.when(client.putItem(any(PutItemRequest.class))).thenThrow(new ResourceNotFoundException("test"));
        Assertions.assertThrows(AwsError.class, () -> repository.save(airTemperature));
        verifySaveByDynamo();
        Mockito.verifyNoMoreInteractions(client);
    }

    private void verifySaveByDynamo() {
        Map<String, AttributeValue> item = Map.of(
            "Pk", new AttributeValue(location),
            "Sk", new AttributeValue(timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)),
            "value", new AttributeValue().withN(String.valueOf(value)));

        Mockito.verify(client).putItem(argThat((PutItemRequest request) -> 
            request.getTableName().equals("MyWeather") && request.getItem().equals(item)));
    }

    @Test
    public void testUpdateHappy() throws Exception {
        UpdateItemResult result = new UpdateItemResult();
        result.setSdkHttpMetadata(SdkHttpMetadata.from(response));

        Mockito.when(client.updateItem(any(UpdateItemRequest.class))).thenReturn(result);

        AirTemperature updated = repository.update(airTemperature);

        Assertions.assertEquals(location, updated.location);
        Assertions.assertEquals(timestamp, updated.timestamp);
        Assertions.assertEquals(value, updated.value);

        verifyUpdateByDynamo();
        Mockito.verifyNoMoreInteractions(client);
    }

    @Test
    public void testUpdateNotFound() {
        
        Mockito.when(client.updateItem(any(UpdateItemRequest.class))).thenThrow(new ConditionalCheckFailedException("test"));
        Assertions.assertThrows(NotFound.class, () -> repository.update(airTemperature));
        verifyUpdateByDynamo();
        Mockito.verifyNoMoreInteractions(client); 
    }

    @Test
    public void testUpdateFails() { 
        Mockito.when(client.updateItem(any(UpdateItemRequest.class))).thenThrow(new ResourceNotFoundException("test"));
        Assertions.assertThrows(AwsError.class, () -> repository.update(airTemperature));
        verifyUpdateByDynamo();
        Mockito.verifyNoMoreInteractions(client);
    }

    private void verifyUpdateByDynamo() {
        Mockito.verify(client).updateItem(argThat((UpdateItemRequest request) -> 
            request.getTableName().equals("MyWeather") &&
                request.getKey().equals(Map.of("Pk", new AttributeValue(location), "Sk", new AttributeValue(timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))) &&
                request.getConditionExpression().equals("attribute_exists(#pk)") && 
                request.getUpdateExpression().equals("SET #value = :newvalue") && 
                request.getExpressionAttributeNames().equals(Map.of("#value", "value", "#pk", "Pk")) &&
                request.getExpressionAttributeValues().equals(Map.of(
                    ":newvalue", new AttributeValue().withN(String.valueOf(value))))
        ));
    }

    @Test
    public void testDeleteHappy() throws Exception {
        DeleteItemResult result = new DeleteItemResult()
            .withAttributes(Map.of(
                "Pk", new AttributeValue(location),
                "Sk", new AttributeValue(timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)),
                "value", new AttributeValue().withN(String.valueOf(value))));
        result.setSdkHttpMetadata(SdkHttpMetadata.from(response));

        Mockito.when(client.deleteItem(any(DeleteItemRequest.class))).thenReturn(result);

        AirTemperature deleted = repository.delete(airTemperature);
        
        Assertions.assertEquals(location, deleted.location);
        Assertions.assertEquals(timestamp, deleted.timestamp);
        Assertions.assertEquals(value, deleted.value);

        verifyDeleteByDynamo();
        Mockito.verifyNoMoreInteractions(client);
    }

    @Test
    public void testDeleteNotFound() {
        Mockito.when(client.deleteItem(any(DeleteItemRequest.class))).thenThrow(new ConditionalCheckFailedException("test"));
        Assertions.assertThrows(NotFound.class, () -> repository.delete(airTemperature));
        verifyDeleteByDynamo();
        Mockito.verifyNoMoreInteractions(client);
    }

    @Test
    public void testDeleteFails() {
        Mockito.when(client.deleteItem(any(DeleteItemRequest.class))).thenThrow(new ResourceNotFoundException("test"));
        Assertions.assertThrows(AwsError.class, () -> repository.delete(airTemperature));
        verifyDeleteByDynamo();
        Mockito.verifyNoMoreInteractions(client);
    }

    private void verifyDeleteByDynamo() {
        Mockito.verify(client).deleteItem(argThat((DeleteItemRequest request) -> 
        request.getTableName().equals("MyWeather") &&
            request.getKey().equals(Map.of(
                "Pk", new AttributeValue(location), 
                "Sk", new AttributeValue(timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))) &&
            request.getConditionExpression().equals("attribute_exists(#pk)") && 
            request.getExpressionAttributeNames().equals(Map.of("#pk", "Pk")) &&
            request.getReturnValues().equals("ALL_OLD")
        ));
    }
}
