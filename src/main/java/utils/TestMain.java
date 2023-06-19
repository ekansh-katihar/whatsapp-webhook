package utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.wnameless.json.flattener.JsonFlattener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class TestMain {
	public static void main(String[] args) throws JsonMappingException, JsonProcessingException {
		String filePath = "RegisterWebhookRequestContext.json";
		String requestContext = readFromFile(filePath);
		Map<String,Object> requestContextMap =
				(Map<String,Object>) new ObjectMapper().readValue(requestContext,Map.class);
		System.out.println(requestContext);
	}
	public static String readFromFile(String filePath) {
        StringBuilder contentBuilder = new StringBuilder();
        
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                contentBuilder.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return contentBuilder.toString();
    }
}
