# Whats app webhook
Either when business sends a message to the customer or when customer reply to the business  `/webhook` is invoked. When customer sends us a message the webhook will show the message sent and the from number. 

# Build
This project depends on `communication-commons` project. Put that jar in the `local-repo` folder and build this project using `mvn clean package`

### Other dependencies:
maven.compiler.source:11

maven.compiler.target:11

Developed with: Apache Maven 3.8.6 Java version: 17.0.5, vendor: Oracle Corporation 

# Simple tests
`WebhookProcessor` and `SimpleChatGPT` have main method to do quick testing. Set the environment variables in IDE(eclipse) > run configuration > Environment
DYNAMO_ACCESS_KEY, DYNAMO_SECRET_KEY, WHATSAPP_TOKEN, WHATSAPP_URL, API_KEY , LOGGING_LEVEL, CHATGPT_URL, TRIAL_MAX_NUMBER_CALLS_PER_DAY, CHATGPT_TOKEN and VERIFY_TOKEN - this will come from facebook graph API and it is only needed for one time. Between, these main methods don't need all these variables :-). 
TODO: Rewrite  these test cases

## Configuration
Deploy it on AWS Lambda (aSaasProduct) with java 11 runtime
Add VERIFY_TOKEN and WHATSAPP_TOKEN to environment variable (See cloud API for whatsapp).



