package api;

import java.util.Map;

import org.json.simple.JSONObject;

import com.amazonaws.services.lambda.runtime.LambdaLogger;

import utils.BasicUtils;

public class WebhookProcessor {
	private final String PREFIX = this.getClass().getName()+" ";
	@SuppressWarnings("unchecked")
	public void whatsAppVerifyWebhookGET(Map<String, Object> requestParams, JSONObject responseJson, LambdaLogger logger) {
		logger.log(PREFIX+"Handling with whatsAppVerifyWebhookGET");
		String mode =  (String)requestParams.get("queryStringParameters[\"hub.mode\"]") ;
		String verify_token = (String) requestParams.get("queryStringParameters[\"hub.verify_token\"]");
		String challenge = (String) requestParams.get("queryStringParameters[\"hub.challenge\"]");
		Map<String, String> envVariable = BasicUtils.getEnvVariable();
		logger.log(PREFIX+"mode, verifyToken, challenge"+mode+verify_token+challenge);
		logger.log(PREFIX+"VERIFY_TOKEN,WHATSAPP_TOKEN"+envVariable.get("VERIFY_TOKEN")+envVariable.get("WHATSAPP_TOKEN"));
		if ("subscribe".equals(mode) && envVariable.get("VERIFY_TOKEN").equals(verify_token)) {
			// Respond with 200 OK and challenge token from the request
			logger.log("WEBHOOK_VERIFIED");
			responseJson.put("statusCode", 200);
			responseJson.put("body", challenge);
		} else {
			logger.log("unverified");
			// Responds with '403 Forbidden' if verify tokens do not match
			responseJson.put("statusCode", 200);
			responseJson.put("body", challenge);
		}

	}
	public void whatsAppVerifyWebhookPOST(Map<String, Object> requestParams, JSONObject responseJson, LambdaLogger logger) {
		String body = (String)requestParams.get("body");
		Map<String, Object> flattenAsMap = BasicUtils.flattenAsMap(body);
		logger.log(PREFIX+"Whatsapp data : ");
		flattenAsMap.entrySet().stream().forEach(e-> logger.log(PREFIX+e.getKey() + " : "+e.getValue()));
		
	}
}
