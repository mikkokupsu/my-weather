package com.myweather.backend;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.BatchWriteItemRequest;
import com.amazonaws.services.dynamodbv2.model.BillingMode;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.PutRequest;
import com.amazonaws.services.dynamodbv2.model.WriteRequest;

import org.apache.commons.collections4.ListUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

@SpringBootApplication
public class MyWeatherBackendApplication {

	private static final Logger logger = LogManager.getLogger(MyWeatherBackendApplication.class);

	public static void main(String[] args) throws Exception {
		ConfigurableApplicationContext context = SpringApplication.run(MyWeatherBackendApplication.class, args);

		final AmazonDynamoDB client = context.getBean(AmazonDynamoDB.class);
		createDynamoDbTable(client);
		createTestData(client);
	}

	private static void createDynamoDbTable(final AmazonDynamoDB client) throws Exception {

		final boolean found = client.listTables().getTableNames().stream().anyMatch(table -> table.equals("MyWeather"));
		if (found) {
			logger.debug("Table exists alread -> skip table creation ");
			return;
		}

		logger.info("Creating DynamoDb table");

		final DynamoDB dynamoDB = new DynamoDB(client);

		final List<AttributeDefinition> attributeDefinitions = new ArrayList<>();
		attributeDefinitions.add(new AttributeDefinition().withAttributeName("Pk").withAttributeType("S"));
		attributeDefinitions.add(new AttributeDefinition().withAttributeName("Sk").withAttributeType("S"));

		final List<KeySchemaElement> keySchema = new ArrayList<>();
		keySchema.add(new KeySchemaElement().withAttributeName("Pk").withKeyType(KeyType.HASH));
		keySchema.add(new KeySchemaElement().withAttributeName("Sk").withKeyType(KeyType.RANGE));

		final CreateTableRequest request = new CreateTableRequest()
			.withTableName("MyWeather")
			.withKeySchema(keySchema)
			.withAttributeDefinitions(attributeDefinitions)
			.withBillingMode(BillingMode.PAY_PER_REQUEST);

		final Table table = dynamoDB.createTable(request);

		table.waitForActive();
	}

	private static void createTestData(final AmazonDynamoDB client) throws Exception {
		final Resource testDataResource = new ClassPathResource("kaisaniemi_202204_1.csv");
		final Scanner scanner = new Scanner(testDataResource.getFile());
		final List<WriteRequest> writeRequests = new ArrayList<>();
		boolean first = true;
		try {
			while (scanner.hasNextLine()) {
				final String[] parts = scanner.nextLine().strip().split(",");
				if (first || parts.length != 6) {
					// Skip header or not enough fields
					first = false;
					continue;
				}
				
				String hour = parts[3].replace(":00", "");
				if (hour.startsWith("0")) {
					// Remove leading zero
					hour = hour.substring(1);
				}

				final LocalDateTime timestamp = LocalDateTime.of(
					Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(hour), 0, 0);
				final float value = Float.parseFloat(parts[5]);
				final String location = "Kaisaniemi";

				Map<String, AttributeValue> item = new HashMap<>();
				item.put("Pk", new AttributeValue(location));
				item.put("Sk", new AttributeValue(timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
				item.put("value", new AttributeValue().withN(String.valueOf(value)));

				writeRequests.add(new WriteRequest(new PutRequest(item)));
			}
		}
		finally {
			scanner.close();
		}

		for (List<WriteRequest> batch : ListUtils.partition(writeRequests, 10)) {
			client.batchWriteItem(new BatchWriteItemRequest(Map.of("MyWeather", batch)));
		}

		logger.info(() -> String.format("Wrote %d item to DynamoDb", writeRequests.size()));
	}
}
