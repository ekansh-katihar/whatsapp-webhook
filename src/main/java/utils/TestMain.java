package utils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import com.github.wnameless.json.flattener.JsonFlattener;
public class TestMain {
	public static void main(String[] args) throws IOException {
		String filePath = "whatsappMessageIncoming.json"; // Replace with your file path
        String json = Files.readString(Paths.get(filePath));
        Map<String, Object> flattenJson = JsonFlattener.flattenAsMap(json);
        flattenJson.entrySet().stream().forEach(e-> System.out.println(e.getKey() + ": "+e.getValue()));
	}
}
