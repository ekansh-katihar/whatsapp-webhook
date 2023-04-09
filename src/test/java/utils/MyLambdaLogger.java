package utils;

import com.amazonaws.services.lambda.runtime.LambdaLogger;

public class MyLambdaLogger {
	private LambdaLogger logger;
	
	public LambdaLogger getLogger() {
		return logger;
	}

	public void setLogger(LambdaLogger logger) {
		this.logger = logger;
	}

	public MyLambdaLogger() {
		logger = new LambdaLogger() {

			@Override
			public void log(byte[] message) {
				System.out.println(new String(message));
			}

			@Override
			public void log(String message) {
				System.out.println(new String(message));
			}
		};
	}
}
