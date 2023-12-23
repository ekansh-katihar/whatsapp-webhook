package openai;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import utils.BasicUtils;

public class SimpleChatGPT {
	private static final Logger logger = Logger.getLogger(SimpleChatGPT.class.getName());
    static {
    	logger.setLevel(BasicUtils.logLevel());
    }
	private String url ;
	private final String PREFIX = this.getClass().getName() + " ";

	public SimpleChatGPT() {
		url =  System.getenv("CHATGPT_URL");
	}

	public String converse(String text,String token) throws Exception {// Build input and API key params

		JSONObject payload = new JSONObject();
		JSONObject message = new JSONObject();
		JSONArray messageList = new JSONArray();

		message.put("role", "user");
		message.put("content", text);
		messageList.add(message);

		payload.put("model", "gpt-3.5-turbo"); // model is important
		payload.put("messages", messageList);
		payload.put("temperature", 0.7);

		StringEntity inputEntity = new StringEntity(payload.toString(), ContentType.APPLICATION_JSON);

		// Build POST request
		HttpPost post = new HttpPost(url);
		post.setEntity(inputEntity);
		post.setHeader("Authorization", "Bearer " + token);
		post.setHeader("Content-Type", "application/json");

		// Send POST request and parse response
		try (CloseableHttpClient httpClient = HttpClients.createDefault();
				CloseableHttpResponse response = httpClient.execute(post)) {
			HttpEntity resEntity = response.getEntity();
			String resJsonString = new String(resEntity.getContent().readAllBytes(), StandardCharsets.UTF_8);
			Map<String, Object> flattenAsMap = BasicUtils.flattenAsMap(resJsonString);
			String chatResponse  = (String)flattenAsMap.get("choices[0].message.content");
			return chatResponse;
		} catch (Exception e) {
			BasicUtils.exceptionTrace(e);
			throw new Exception("Error while interacting with ChatGPT");
		}
	}

	public static void main(String[] args) throws Exception {
		SimpleChatGPT ai = new SimpleChatGPT();
		ai.url="https://api.openai.com/v1/chat/completions";
		String apiKey = System.getProperty("API_KEY");
		if(apiKey == null) {
			System.out.println("set API_KEY obtained from openAI (or use information file) using -DAPI_KEY=sk-Q.........................");
		}
		String converse = ai.converse("What is the difference between bonds and stocks",apiKey);
		System.out.println("======");
		System.out.println(converse);
	}
}
