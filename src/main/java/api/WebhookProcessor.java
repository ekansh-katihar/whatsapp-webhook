package api;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.json.simple.JSONObject;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.registration.repository.UserInfoRepositoryImpl;

import com.registration.entity.UserNumber;
import openai.ChatGPT;
import openai.SimpleChatGPT;
import utils.BasicUtils;

public class WebhookProcessor {
	private final String PREFIX = this.getClass().getName() + " ";
	private UserInfoRepositoryImpl userInfoRepository = UserInfoRepositoryImpl.getInstance();
	@SuppressWarnings("unchecked")
	public void whatsAppVerifyWebhookGET(Map<String, Object> requestParams, JSONObject responseJson,
			LambdaLogger logger) {
		logger.log(PREFIX + "Handling with whatsAppVerifyWebhookGET");
		String mode = (String) requestParams.get("queryStringParameters[\"hub.mode\"]");
		String verify_token = (String) requestParams.get("queryStringParameters[\"hub.verify_token\"]");
		String challenge = (String) requestParams.get("queryStringParameters[\"hub.challenge\"]");
		Map<String, String> envVariable = BasicUtils.getEnvVariable();
		logger.log(PREFIX + "mode, verifyToken, challenge" + mode + verify_token + challenge);
		logger.log(PREFIX + "VERIFY_TOKEN,WHATSAPP_TOKEN" + envVariable.get("VERIFY_TOKEN")
				+ envVariable.get("WHATSAPP_TOKEN"));
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

	public void whatsAppVerifyWebhookPOST(Map<String, Object> requestParams, JSONObject responseJson,
			LambdaLogger logger) {
		String body = (String) requestParams.get("body");
		Map<String, Object> flattenAsMap = BasicUtils.flattenAsMap(body);
		logger.log(PREFIX + "Whatsapp data : ");
		flattenAsMap.entrySet().stream().forEach(e -> logger.log(PREFIX + e.getKey() + " : " + e.getValue()));
		String textBody = (String) flattenAsMap.get("entry[0].changes[0].value.messages[0].text.body");
		String phoneNumber = (String) flattenAsMap.get("entry[0].changes[0].value.messages[0].from");
		logger.log(PREFIX + "textBody = " + textBody+",phoneNumber =  " + phoneNumber);
		if(textBody == null ||phoneNumber == null ) return;
		Optional<UserNumber> findByPhoneNumber = userInfoRepository.findByPhoneNumber(phoneNumber);
		if(findByPhoneNumber.isEmpty() || !verifyUser(logger, findByPhoneNumber.get())){
			return;
		}
		UserNumber userNumber = findByPhoneNumber.get();
		userNumber.setCalls(userNumber.getCalls()+1);
		
		SimpleChatGPT chat = new SimpleChatGPT(logger);
		String aiResponse = chat.converse(textBody);
		try {
			String whatsAppResponse = sendMessage(aiResponse, phoneNumber);
			logger.log("whatsAppResponse = " + whatsAppResponse);
		} catch (IOException | InterruptedException | URISyntaxException e1) {
			logger.log("Exception = " + BasicUtils.exceptionTrace(e1));
		}
		userInfoRepository.saveUserNumber(userNumber);
	}

	private boolean verifyUser(LambdaLogger logger, UserNumber userNumber) {
		if (userNumber == null) {
			logger.log("Cant find the user.");
			return false;
		}
		if (userNumber.getSubscriptionEndDate() == null) {
			logger.log("Date is null!!!");
			return false;
		}
		if (userNumber.getSubscriptionEndDate().before(new Date())) {
			logger.log("Subscription ended");
			return false;
		}
		return true;
	}

	public String sendMessage(String message, String phoneNumber)
			throws IOException, InterruptedException, URISyntaxException {
		String token =  System.getenv("WHATSAPP_TOKEN");
		String url = System.getenv("WHATSAPP_URL");
		Map<String, Object> map = new HashMap<>();
		map.put("messaging_product", "whatsapp");
		map.put("recipient_type", "individual");
		map.put("to", phoneNumber);
		map.put("type", "text");
		Map<String, Object> messageMap = new HashMap<>();
		messageMap.put("preview_url", true);
		messageMap.put("body", message);
		map.put("text", messageMap);
		String json = new ObjectMapper().writeValueAsString(map);

		HttpRequest request = HttpRequest.newBuilder().uri(new URI(url)).header("Authorization", "Bearer " + token)
				.header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(json)).build();
		HttpClient http = HttpClient.newHttpClient();
		HttpResponse<String> response = http.send(request, BodyHandlers.ofString());
		String body = response.body();
		return body;
	}

	public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException {
		new WebhookProcessor().sendMessage("Hello", "");
	}
}
