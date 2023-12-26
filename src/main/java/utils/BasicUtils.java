package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.json.simple.JSONObject;
import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.github.wnameless.json.flattener.JsonFlattener;

import api.Webhook;

public class BasicUtils {
	private static final  String PREFIX = "utils.BasicUtils ";
//	public static LambdaLogger logger;
	private static final Logger logger = Logger.getLogger(BasicUtils.class.getName());
	public static Map<String, Object> getRequestParams(InputStream input) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(input));
		String json = reader.lines().collect(Collectors.joining());
		logger.log(Level.FINE ,  "incoming json = " + json);
		Map<String, Object> flattenJson = JsonFlattener.flattenAsMap(json);
		logger.log(Level.FINE ,  "map representation = ");
//		flattenJson.entrySet().stream().forEach(e -> logger.log(Level.INFO ,  e.getKey() + ": " + e.getValue()));
		return flattenJson;
	}

	public static Map<String, Object> flattenAsMap(String json) {
		return JsonFlattener.flattenAsMap(json);
	}

	public static Map<String, JSONObject> getRequestParams1(InputStream input) throws IOException, ParseException {
		Map<String, JSONObject> params = new HashMap<String, JSONObject>() {
			{
				put("pathParameters", new JSONObject());
				put("queryStringParameters", new JSONObject());
			}
		};

		JSONParser parser = new JSONParser();
		BufferedReader reader = new BufferedReader(new InputStreamReader(input));

		JSONObject event = (JSONObject) parser.parse(reader);
		if (event.get("pathParameters") != null) {
			JSONObject pps = (JSONObject) event.get("pathParameters");
			params.put("pathParameters", pps);
		}
		if (event.get("queryStringParameters") != null) {
			JSONObject qsp = (JSONObject) event.get("queryStringParameters");
			params.put("queryStringParameters", qsp);
		}
		if (event.get("requestContext") != null) {
			JSONObject context = (JSONObject) event.get("requestContext");
			params.put("requestContext", context);
		}

//		for(Object k : event.keySet()) {
//			logger.log("DEBUG "+k.getClass() +" key "+k+" : value "+event.get(k)+" ; class "+event.get(k));
//		}
//		logger.log("DEBUG "+event);
//		logger.log("DEBUG "+event.get("hub.mode"));
//		logger.log("DEBUG "+event.get("hub.verify_token"));
//		logger.log("DEBUG "+event.get("hub.challenge"));
		return params;
	}

	public static Map jsonToMap(String text) {
		Map map = new LinkedHashMap();
		JSONParser parser = new JSONParser();

		ContainerFactory containerFactory = new ContainerFactory() {
			@Override
			public Map createObjectContainer() {
				return new LinkedHashMap<>();
			}

			@Override
			public List creatArrayContainer() {
				return new LinkedList<>();
			}
		};
		try {
			map = (Map) parser.parse(text, containerFactory);
			map.forEach((k, v) -> logger.log(Level.FINE,"Key : " + k + " Value : " + v));
		} catch (ParseException pe) {
			logger.log(Level.WARNING,"position: " + pe.getPosition());
			logger.log(Level.WARNING,exceptionTrace(pe));
		}
		return map;
	}

	public static Level logLevel() {
		String logLevel = System.getenv("LOGGING_LEVEL") == null?"INFO": System.getenv("LOGGING_LEVEL");
		Level level = Level.INFO;
		switch(logLevel) {
			case "INFO": level= Level.INFO; break;
			case "FINE": level=  Level.FINE; break;
			case "DEBUG": level=  Level.FINE; break;
			case "WARNING": level=  Level.WARNING; break;
			case "ERROR": level=  Level.SEVERE; break;
			default: level = Level.INFO;
		}
		return level;
	}
	public static Map<String, String> getEnvVariable() {
		Map<String, String> map = new HashMap<>();
		
		map.put("WHATSAPP_TOKEN", System.getenv("WHATSAPP_TOKEN"));
		map.put("VERIFY_TOKEN", System.getenv("VERIFY_TOKEN"));
		String getenv = System.getenv("LOGGING_LEVEL");
		
		map.put("LOGGING_LEVEL", System.getenv("LOGGING_LEVEL")==null ||System.getenv("LOGGING_LEVEL").isBlank()
					?"INFO":System.getenv("LOGGING_LEVEL"));
		return map;
	}

	public static String exceptionTrace(Exception e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		return sw.toString();
	}

}
