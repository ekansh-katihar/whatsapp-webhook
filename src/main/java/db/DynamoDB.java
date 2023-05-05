package db;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.amazonaws.services.lambda.runtime.LambdaLogger;

import entity.UserNumber;
import utils.BasicUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DynamoDB {
	private final String PREFIX = this.getClass().getName() + " ";
	private LambdaLogger logger;

	public DynamoDB(LambdaLogger logger) {
		this.logger = logger;
	}

	public UserNumber getUserNumber(String table_name, String phoneNumber) {
		final AmazonDynamoDB dynamoDB = AmazonDynamoDBClientBuilder.standard().withRegion(Regions.US_EAST_2).build();
		GetItemRequest request = new GetItemRequest();
		request.setTableName(table_name);
		//select specific attributes
//		request.setProjectionExpression("phoneNumber");

		Map<String, AttributeValue> keysMap = new HashMap<>();
		keysMap.put("phoneNumber", new AttributeValue(phoneNumber));

		request.setKey(keysMap);
		UserNumber userNumber = null;
		try {
			/* Send Get Item Request */
			GetItemResult result = dynamoDB.getItem(request);
			userNumber = createUser(result);
		} catch (AmazonServiceException e) {
			logger.log(PREFIX+" Exception while getting data from DynamoDb. "+BasicUtils.exceptionTrace(e));
			return null;
		}
		return userNumber;
	}

	private  UserNumber createUser(GetItemResult result) {

		UserNumber userNumber = new UserNumber();

		Map<String, AttributeValue> item = result.getItem();
		if( item == null) { 
			logger.log(PREFIX+"No record found.");
			return null;
		}
		userNumber.setPhoneNumber(item.get("phoneNumber").getS());
		userNumber.setPrincipal(item.get("principal").getS());

		try {
			String stringDate = item.get("subscriptionEndDate").getS();
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			Date date = formatter.parse(stringDate);
			userNumber.setSubscriptionEndDate(date);
			String n = item.get("calls").getN();
			Long calls = Long.valueOf(n);
			userNumber.setCalls(calls);
		} catch (ParseException | NumberFormatException e) {
			logger.log(PREFIX+"Exception occured. "+BasicUtils.exceptionTrace(e));
			return null;
 		}
		return userNumber;
	}

	public static void main(String[] args) {
//		UserNumber userNumber = new DynamoDB().getUserNumber("user_number", "14388229758");
//		if(userNumber.getSubscriptionEndDate().before(  new Date())) {
//			System.out.println("Subscription ended");
//		}
//		System.out.println(userNumber);
	}
}