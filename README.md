# Whats app webhook
Either when business sends a message to the customer or when customer reply to the business  `/webhook` is invoked. When customer sends us a message the webhook will show the message sent and the from number. 

## Configuration
Deploy it on AWS Lambda with java 1.8 runtime
Add VERIFY_TOKEN and WHATSAPP_TOKEN to environment variable (See cloud API for whatsapp).

## To build a deployable package on lambda

mvn clean package


