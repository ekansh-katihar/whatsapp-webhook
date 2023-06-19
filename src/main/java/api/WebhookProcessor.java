package api;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.simple.JSONObject;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.registration.repository.UserInfoRepositoryImpl;
import com.registration.utils.Constants;
import com.registration.entity.UserNumber;
import openai.ChatGPT;
import openai.SimpleChatGPT;
import utils.BasicUtils;

public class WebhookProcessor {
	private static final Logger logger = Logger.getLogger(WebhookProcessor.class.getName());
	static {
		logger.setLevel(BasicUtils.logLevel());
	}
	private final String PREFIX = this.getClass().getName() + " ";
	private UserInfoRepositoryImpl userInfoRepository = UserInfoRepositoryImpl.getInstance();

	@SuppressWarnings("unchecked")
	public void whatsAppVerifyWebhookGET(Map<String, Object> queryStringParameters, Map<String, String> response) {
		logger.log(Level.INFO, "Handling with whatsAppVerifyWebhookGET");
		String mode = (String) queryStringParameters.get("hub.mode");
		String verify_token = (String) queryStringParameters.get("hub.verify_token");
		String challenge = (String) queryStringParameters.get("hub.challenge");
		Map<String, String> envVariable = BasicUtils.getEnvVariable();
		logger.log(Level.INFO, "mode, verifyToken, challenge:" + mode + " , " + verify_token + " , " + challenge);
		logger.log(Level.INFO,
				"VERIFY_TOKEN,WHATSAPP_TOKEN" + envVariable.get("VERIFY_TOKEN") + envVariable.get("WHATSAPP_TOKEN"));
		if ("subscribe".equals(mode) && envVariable.get("VERIFY_TOKEN").equals(verify_token)) {
			// Respond with 200 OK and challenge token from the request
			logger.log(Level.INFO, "WEBHOOK_VERIFIED");
			response.put(Constants.STATUS, Constants.STATUS_SUCCESS);
			response.put("statusCode", "200");
			response.put("body", challenge);
		} else {
			logger.log(Level.INFO, "unverified");
			// Responds with '403 Forbidden' if verify tokens do not match
			response.put(Constants.STATUS, Constants.STATUS_FAIL);
			response.put(Constants.STATUS_CODE_INTERNAL, Constants.STATUS_CODE_FORBIDDEN);
			response.put(Constants.STATUS_DESC, "Token mismatch");
			response.put("statusCode", "403");
//			response.put("body", challenge);
		}

	}

	public void whatsAppVerifyWebhookPOST(Map<String, Object> requestParams, Map<String, String> response) {
		logger.log(Level.FINE, "Whatsapp data : ");
		String body = (String) requestParams.get("body");
		Map<String, Object> flattenAsMap = BasicUtils.flattenAsMap(body);
		logger.log(Level.FINER, "Whatsapp data as K,V pair: ");
		flattenAsMap.entrySet().stream().forEach(e -> logger.log(Level.FINE, e.getKey() + " : " + e.getValue()));
		String textBody = (String) flattenAsMap.get("entry[0].changes[0].value.messages[0].text.body");
		String phoneNumber = (String) flattenAsMap.get("entry[0].changes[0].value.messages[0].from");
		logger.log(Level.INFO, "textBody = " + textBody + ",phoneNumber =  " + phoneNumber);

		if (invalidData(textBody, phoneNumber, response)) {
			return;
		}
		Optional<UserNumber> findByPhoneNumber = userInfoRepository.findByPhoneNumber(phoneNumber);
		if (dataNotFoundInDb(findByPhoneNumber, response)) {
			return;
		}
		UserNumber userNumber = findByPhoneNumber.get();
		if (quotaExceeded(userNumber, response)) {
			return;
		}

		userNumber.setTotalCalls(userNumber.getTotalCalls() + 1);
		userNumber.setCalls(userNumber.getCalls() + 1);

		SimpleChatGPT chat = new SimpleChatGPT();
		try {
			String aiResponse = chat.converse(textBody);
			sendMessage(aiResponse, phoneNumber);
			userInfoRepository.saveUserNumber(userNumber);
			response.put(Constants.STATUS, Constants.STATUS_SUCCESS);
			response.put("statusCode", "200");// Expected by whatsapp
		} catch (Exception e1) {
			logger.log(Level.WARNING, "Exception = " + BasicUtils.exceptionTrace(e1));
			response.put(Constants.STATUS, Constants.STATUS_FAIL);
			response.put(Constants.STATUS_CODE_INTERNAL, Constants.STATUS_FAIL);
			response.put(Constants.STATUS_DESC, "Error at chatgpt or whatsapp");
			response.put("statusCode", "200");// Expected by whatsapp
		}
	}

	private boolean quotaExceeded(UserNumber userNumber, Map<String, String> response) {
		Date updatedAt = userNumber.getUpdatedAt();
		Date todayDate = new Date();
		if (updatedAt == null) {
			response.put(Constants.STATUS, Constants.STATUS_FAIL);
			response.put(Constants.STATUS_CODE_INTERNAL, Constants.STATUS_QUOTA_EXCEEDED);
			response.put(Constants.STATUS_DESC, "Quota can't be calculated as updatedAt was null");
			response.put("statusCode", "200");// Expected by whatsapp
			logger.log(Level.SEVERE, "updatedAt is null for the User " + userNumber.getPhoneNumber());
			return true;
		}
		if ("TRIAL".equals(userNumber.getSubscriptionType())) {
			Calendar updatedAtCal = Calendar.getInstance();
			updatedAtCal.setTime(updatedAt);

			Calendar todayDateCal2 = Calendar.getInstance();
			todayDateCal2.setTime(todayDate);

			// Compare the year, month, and day components
			boolean sameDate = updatedAtCal.get(Calendar.YEAR) == todayDateCal2.get(Calendar.YEAR)
					&& updatedAtCal.get(Calendar.MONTH) == todayDateCal2.get(Calendar.MONTH)
					&& updatedAtCal.get(Calendar.DAY_OF_MONTH) == todayDateCal2.get(Calendar.DAY_OF_MONTH);

			if (sameDate && userNumber.getCalls() > Integer.parseInt(System.getenv("TRIAL_MAX_NUMBER_CALLS_PER_DAY"))) {
				response.put(Constants.STATUS, Constants.STATUS_FAIL);
				response.put(Constants.STATUS_CODE_INTERNAL, Constants.STATUS_QUOTA_EXCEEDED);
				response.put(Constants.STATUS_DESC, "Maximum calls reached");
				response.put("statusCode", "200");// Expected by whatsapp
				logger.log(Level.INFO, "Maximum calls reached for " + userNumber.getPhoneNumber());
				return true;
			} else if (!sameDate) {
				logger.log(Level.INFO, "Resetting daily maximum" + userNumber.getPhoneNumber());
				userNumber.setCalls(0);
			}
		}
		return false;
	}

	private boolean verifyUser(UserNumber userNumber) {
		if (userNumber == null) {
			logger.log(Level.INFO, "Cant find the user.");
			return false;
		}
		if (userNumber.getSubscriptionEndDate() == null) {
			logger.log(Level.INFO, "Date is null!!!");
			return false;
		}
		if (userNumber.getSubscriptionEndDate().before(new Date())) {
			logger.log(Level.INFO, "Subscription ended");
			return false;
		}
		return true;
	}

	public String sendMessage(String message, String phoneNumber)
			throws IOException, InterruptedException, URISyntaxException {
		
		logger.log(Level.INFO, "Sending response on WhatsApp" );
		String token = System.getenv("WHATSAPP_TOKEN");
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
		logger.log(Level.INFO, "Sent response on WhatsApp" );
		logger.log(Level.FINE, "Sent response on WhatsApp "+body );
		return body;
	}

	private boolean invalidData(String textBody, String phoneNumber, Map<String, String> response) {
		if (textBody == null || phoneNumber == null) {
			response.put(Constants.STATUS, Constants.STATUS_FAIL);
			response.put(Constants.STATUS_CODE_INTERNAL, Constants.STATUS_CODE_INVALID_INPUT);
			response.put(Constants.STATUS_DESC, "Phone number or textbody was null");
			logger.log(Level.INFO, "Phone number or textbody was null");
			response.put("statusCode", "200");// Expected by whatsapp
			return true;
		}

		return false;
	}

	private boolean dataNotFoundInDb(Optional<UserNumber> findByPhoneNumber, Map<String, String> response) {
		if (findByPhoneNumber.isEmpty() || !verifyUser(findByPhoneNumber.get())) {
			response.put(Constants.STATUS, Constants.STATUS_FAIL);
			response.put(Constants.STATUS_CODE_INTERNAL, Constants.STATUS_CODE_DATA_NOT_FOUND);
			response.put(Constants.STATUS_DESC, "Data not found in the database");
			response.put("statusCode", "200");// Expected by whatsapp
			logger.log(Level.SEVERE, "Data not found in the database");
			return true;
		}
		return false;
	}

	public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException {
		new WebhookProcessor().sendMessage("Hello", "");
	}
}
