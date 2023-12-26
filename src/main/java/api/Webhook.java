package api;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.reflect.TypeToken;
import com.registration.utils.Constants;

import utils.BasicUtils;

public class Webhook implements RequestHandler<Map<String, Object>, Map<String, String>> {
	private final String PREFIX = this.getClass().getName() + " ";
    private static final Logger logger = Logger.getLogger(Webhook.class.getName());
    static {
    	logger.setLevel(BasicUtils.logLevel());
    }
	@Override
	public Map<String, String> handleRequest(Map<String, Object> requestParams, Context context) {
		Map<String, String> response = new HashMap<>();
		
		WebhookProcessor processor = new WebhookProcessor();
		logger.log(Level.INFO , "Whatsapp WEBHOOK Invoked");
		requestParams.entrySet().stream().forEach(e -> logger.log(Level.FINE ,PREFIX+e.getKey() + ": " + e.getValue()));
		Map<String,Object>  queryStringParameters = (Map<String,Object>) requestParams.get("queryStringParameters");
		Map<String,Object>  requestContext = (Map<String,Object>) requestParams.get("requestContext");
		Map<String,Object>  httpMap = (Map<String,Object>) requestContext.get("http");
		String method = (String)httpMap.get("method");
		String path = (String)httpMap.get("path");
		
		if ("GET".equals(method) && "/webhook".equals(path)) {
			logger.log(Level.INFO ,"GET Invoked Webhook.handleRequest");
			processor.whatsAppVerifyWebhookGET(queryStringParameters, response);
		} else if ("POST".equals(method)
				&& "/webhook".equals(path)) {
			logger.log(Level.INFO ,"POST Invoked Webhook.handleRequest");
			processor.whatsAppVerifyWebhookPOST(requestParams, response);
		}
		logger.log(Level.INFO ,"Returning with "+response.get("statusCode")+"::"+response.get(Constants.STATUS_DESC));
		return response;

	}

}
