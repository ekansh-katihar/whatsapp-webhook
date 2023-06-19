package openai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionChunk;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;

import io.reactivex.Flowable;
import utils.BasicUtils;
import utils.LambdaLoggerImpl;

public class ChatGPT {
	private static final Logger logger = Logger.getLogger(ChatGPT.class.getName());
    static {
    	logger.setLevel(BasicUtils.logLevel());
    }
	private final String PREFIX = this.getClass().getName() + " ";
	public ChatGPT() {
	
	}

	
	public String converse(String textBody) {
		logger.log(Level.INFO , "Conversing with ChatGPT: "+textBody);
		String token =  System.getenv("CHATGPT_ENV");
		OpenAiService service = new OpenAiService(token);

		final List<ChatMessage> messages = new ArrayList<>();
		final ChatMessage systemMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(),
				textBody);
		messages.add(systemMessage);
		ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder().model("gpt-3.5-turbo")
				.messages(messages).n(1).maxTokens(50).logitBias(new HashMap<>()).build();

		Flowable<ChatCompletionChunk> streamChatCompletion = service.streamChatCompletion(chatCompletionRequest);
		
		final StringBuilder sb = new StringBuilder();
		final StringBuilder finishReason = new StringBuilder();
		streamChatCompletion.blockingForEach(b -> {
			List<ChatCompletionChoice> choices = b.getChoices();
 			choices.forEach(e -> {
 				sb.append(e.getMessage().getContent());
 				finishReason.delete(0, finishReason.length());
 	 			finishReason.append(e.getFinishReason());
 				});
		});
		String str = sb.toString();
		logger.log(Level.INFO , "finished responding: "+str);
		return str;

	}

	public static void main(String[] args) {
		ChatGPT ai = new ChatGPT();
		String converse = ai.converse("What is the difference between bonds and stocks");
		System.out.println("======");
		System.out.println(converse);
	}
}
