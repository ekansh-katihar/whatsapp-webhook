package openai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionChunk;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;

import io.reactivex.Flowable;
import utils.LambdaLoggerImpl;

public class ChatGPT {
	private LambdaLogger logger;
	private final String PREFIX = this.getClass().getName() + " ";
	public ChatGPT(LambdaLogger logger) {
		this.logger =  logger;
	}

	public String converse(String textBody) {
		logger.log(PREFIX+"Conversing with ChatGPT: "+textBody);
		String token = "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";
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
		logger.log(PREFIX+"finished responding: "+str);
		return str;

	}

	public static void main(String[] args) {
		ChatGPT ai = new ChatGPT(new LambdaLoggerImpl());
		String converse = ai.converse("continue");
		System.out.println("======");
		System.out.println(converse);
	}
}
