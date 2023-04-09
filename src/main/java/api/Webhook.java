package api;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.github.wnameless.json.flattener.JsonFlattener;

import utils.BasicUtils;

public class Webhook implements RequestStreamHandler {
	private final String PREFIX = this.getClass().getName()+" ";
	private final String REQUEST_TYPE = "requestContext.http.method";
	private final String REQUEST_PATH = "requestContext.http.path";
	private final String isIncomingMessageKey = "entry[0].changes[0].value.messages[0].from";
	@SuppressWarnings("unchecked")
	@Override
	public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
		WebhookProcessor processor = new WebhookProcessor();
		JSONObject responseJson = new JSONObject();
		LambdaLogger logger = context.getLogger();
		BasicUtils.logger= logger;
		logger.log(PREFIX+"Invoked Webhook.handleRequest");
		Map<String, Object> requestParams = BasicUtils.getRequestParams(input);
		if ( "GET".equals(requestParams.get(REQUEST_TYPE)) 
				&& "/webhook".equals(requestParams.get(REQUEST_PATH))
				) {
			logger.log(PREFIX+"GET Invoked Webhook.handleRequest");
			processor.whatsAppVerifyWebhookGET(requestParams, responseJson, logger);
		}else if ( "POST".equals(requestParams.get(REQUEST_TYPE)) 
				&& "/webhook".equals(requestParams.get(REQUEST_PATH))
				) {
			logger.log(PREFIX+"POST Invoked Webhook.handleRequest");
			processor.whatsAppVerifyWebhookPOST(requestParams, responseJson, logger);
		}
		
		logger.log(PREFIX+"DONE");
		
		 
		
		

		 
		OutputStreamWriter writer = new OutputStreamWriter(output, "UTF-8");
		writer.write(responseJson.toString());
		writer.close();
		logger.log(PREFIX+"RESPONDED");

	}

	

}
