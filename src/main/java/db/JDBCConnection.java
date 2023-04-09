package db;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import utils.BasicUtils;

public class JDBCConnection {
	static Map<Character, Integer> charsToInt = new HashMap<>();
	static Map<Integer, Character> intToChar = new HashMap<>();
	private static final int RANGE = 63, BASE = 4;
	private static final long MAX = 63;
	private static final String MAX_RANGE = "ddd";
	private static Connection conn = null;

	public static int createMap(char start, char end, int pos) {
		for (char i = start; i <= end; i++) {
			charsToInt.put(i, pos);
			intToChar.put(pos, i);
			pos++;
		}
		return pos;
	}

	public static String getEndRange(LambdaLogger logger) throws SQLException, ClassNotFoundException {
		logger.log("Invoked JDBCSample.getEndRange -1");
		String end = "akaaapu";
		getConnection(logger);
		logger.log("executing a query: SELECT end_range FROM ranges where id=(select MAX(id) from ranges) ");
		Statement stmt = conn.createStatement();
		ResultSet resultSet = stmt.executeQuery("SELECT end_range FROM ranges where id=(select MAX(id) from ranges)");
		logger.log("Got the result set");
		if (resultSet.next()) {
			end = resultSet.getObject(1).toString();
		}
		logger.log("Successfully executed query.  Result: " + end);
		return end;
	}

	public static void insertNewRange(Range range, LambdaLogger logger) throws ClassNotFoundException, SQLException {

		getConnection(logger);
		String query = "insert into ranges(ec2_id,start_range,end_range,status) values(?,?,?,?)";
		PreparedStatement preparedStatement = conn.prepareStatement(query);
		preparedStatement.setString(1, range.getEc2Id());
		preparedStatement.setString(2, range.getStartRange());
		preparedStatement.setString(3, range.getEndRange());
		preparedStatement.setString(4, range.getStatus());

		preparedStatement.executeUpdate();

	}

	public static Connection getConnection(LambdaLogger logger) throws SQLException, ClassNotFoundException {
		if (conn != null)
			return conn;
		Class.forName("org.postgresql.Driver");
		logger.log("getting secret for DB connection");
//		String secret = BasicUtils.getSecret();
		Map<String, String> jsonToMap = BasicUtils.getEnvVariable();
		String db = (String) jsonToMap.get("dbInstanceIdentifier");
		String username = (String) jsonToMap.get("username");
		String password = (String) jsonToMap.get("password");
		String port = (String) jsonToMap.get("port");
		String host = (String) jsonToMap.get("host");
		String connectionString = "jdbc:postgresql://" + host + ":" + port + "/" + db;
//		conn = DriverManager.getConnection(
//				"jdbc:postgresql://shortly.cqrni4n4j4dx.us-east-2.rds.amazonaws.com:5432/shortly", "root",
//				"Welcome1");
		logger.log("getting a db connection "+connectionString+"/"+username+"/"+password);
		conn = DriverManager.getConnection(connectionString, username, password);
		return conn;
	}

	public static void closeConnection(LambdaLogger logger) {
		try {
			conn.close();
		} catch (SQLException e) {
			logger.log(BasicUtils.exceptionTrace(e));
		}

	}

}
