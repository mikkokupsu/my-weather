package com.myweather.backend.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.myweather.backend.database.model.AirTemperature;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DynamoDbUtil {

    private static final Logger logger = LogManager.getLogger(DynamoDbUtil.class);

    private DynamoDbUtil() {
        // NO-OP
    }

    public static Map<String, AttributeValue> toDynamoDbItem(final AirTemperature airTemperature) {
        return toDynamoDbItem(airTemperature, false);
    }

    public static Map<String, AttributeValue> toDynamoDbItem(final AirTemperature airTemperature, final boolean onlyKeys) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("Pk", new AttributeValue(airTemperature.location));
        item.put("Sk", new AttributeValue(airTemperature.timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
        if (!onlyKeys) {
            item.put("value", new AttributeValue().withN(String.valueOf(airTemperature.value)));
        }
        return item;
    }

    public static AirTemperature fromDynamoDbItem(final Map<String, AttributeValue> item) {
        for (Entry<String, AttributeValue> entry : item.entrySet()) {
            logger.debug(() -> String.format("%s=%s", entry.getKey(), entry.getValue().toString()));
        }

        String location = item.get("Pk").getS();
        LocalDateTime timestamp = LocalDateTime.parse(item.get("Sk").getS(), DateTimeFormatter.ISO_LOCAL_DATE_TIME); 
        double value = Double.parseDouble(item.get("value").getN());

        return new AirTemperature(timestamp, value, location);
    }
}
