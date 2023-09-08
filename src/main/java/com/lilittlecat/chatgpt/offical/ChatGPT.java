package com.lilittlecat.chatgpt.offical;

import static com.lilittlecat.chatgpt.offical.entity.Constant.DEFAULT_CHAT_COMPLETION_API_URL;
import static com.lilittlecat.chatgpt.offical.entity.Constant.DEFAULT_MODEL;
import static com.lilittlecat.chatgpt.offical.entity.Constant.DEFAULT_USER;
import static com.lilittlecat.chatgpt.offical.entity.Constant.DEFAULT_MAX_TOKENS;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lilittlecat.chatgpt.offical.entity.ChatCompletionRequestBody;
import com.lilittlecat.chatgpt.offical.entity.ChatCompletionResponseBody;
import com.lilittlecat.chatgpt.offical.entity.Message;
import com.lilittlecat.chatgpt.offical.entity.Model;
import com.lilittlecat.chatgpt.offical.exception.BizException;
import com.lilittlecat.chatgpt.offical.exception.Error;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * <p>
 * a Java client for ChatGPT uses official API.
 * </p>
 *
 * @author <a href="https://github.com/LiLittleCat">LiLittleCat</a>
 * @since 2023/3/2
 */
@Slf4j
@Builder
public class ChatGPT {
    private final String apiKey;
    private String apiHost = DEFAULT_CHAT_COMPLETION_API_URL;
    protected OkHttpClient client;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Map<String,String> httpHeaders;

    public ChatGPT(String apiKey) {
        this.apiKey = apiKey;
        this.client = new OkHttpClient();
    }

    public ChatGPT(String apiKey, OkHttpClient client) {
        this.apiKey = apiKey;
        this.client = client;
    }

    public ChatGPT(String apiKey, Proxy proxy) {
        this.apiKey = apiKey;
        client = new OkHttpClient.Builder().proxy(proxy).build();
    }

    public ChatGPT(String apiKey, String proxyHost, int proxyPort) {
        this.apiKey = apiKey;
        client = new OkHttpClient.Builder().
                proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort)))
                .build();
    }

    public ChatGPT(String apiHost, String apiKey) {
        this.apiHost = apiHost;
        this.apiKey = apiKey;
        this.client = new OkHttpClient();
    }

    public ChatGPT(String apiHost, String apiKey, OkHttpClient client) {
        this.apiHost = apiHost;
        this.apiKey = apiKey;
        this.client = client;
    }
    
    public ChatGPT(String apiHost, String apiKey, OkHttpClient client, Map<String,String> httpHeaders) {
        this.apiHost = apiHost;
        this.apiKey = apiKey;
        this.client = client;
        this.httpHeaders = httpHeaders;
    }

    public ChatGPT(String apiHost, String apiKey, Proxy proxy) {
        this.apiHost = apiHost;
        this.apiKey = apiKey;
        client = new OkHttpClient.Builder().proxy(proxy).build();
    }

    public ChatGPT(String apiHost, String apiKey, String proxyHost, int proxyPort) {
        this.apiHost = apiHost;
        this.apiKey = apiKey;
        client = new OkHttpClient.Builder().
                proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort)))
                .build();
    }


    public String ask(String input) {
        return ask(DEFAULT_MODEL.getName(), DEFAULT_USER, input, DEFAULT_MAX_TOKENS);
    }

    public String ask(String user, String input) {
        return ask(DEFAULT_MODEL.getName(), user, input, DEFAULT_MAX_TOKENS);
    }

    public String ask(Model model, String input) {
        return ask(model.getName(), DEFAULT_USER, input, DEFAULT_MAX_TOKENS);
    }

    public String ask(List<Message> messages) {
        return ask(DEFAULT_MODEL.getName(), DEFAULT_USER, messages, DEFAULT_MAX_TOKENS);
    }

    public String ask(Model model, List<Message> messages) {
        return ask(model.getName(), DEFAULT_USER, messages, DEFAULT_MAX_TOKENS);
    }

    public String ask(String model, String user, List<Message> message, Integer maxTokens) {
        ChatCompletionResponseBody chatCompletionResponseBody = askOriginal(model, user, message, maxTokens);
        List<ChatCompletionResponseBody.Choice> choices = chatCompletionResponseBody.getChoices();
        StringBuilder result = new StringBuilder();
        for (ChatCompletionResponseBody.Choice choice : choices) {
            result.append(choice.getMessage().getContent());
        }
        return result.toString();
    }

    public String ask(Model model, String user, String input, Integer maxTokens) {
        return ask(model.getName(), user, input, maxTokens);
    }

    private String buildChatRequestBody(String model, String user, List<Message> messages, Integer maxTokens) {
        try {
            ChatCompletionRequestBody.ChatCompletionRequestBodyBuilder builder = ChatCompletionRequestBody.builder()
                    .model(model)
                    .messages(messages)
                    .user(user);
            
            if(maxTokens>0)
                   builder.maxTokens(maxTokens);
 
            ChatCompletionRequestBody requestBody = builder.build();
            return objectMapper.writeValueAsString(requestBody);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * ask for response message
     *
     * @param model model
     * @param role  role
     * @param input input
     * @return ChatCompletionResponseBody
     */
    /*public ChatCompletionResponseBody askOriginal(String model, String role, String input) {
        return askOriginal(model, Collections.singletonList(Message.builder()
                .role(role)
                .content(input)
                .build()));
    }*/

    /**
     * ask for response message
     *
     * @param model    model
     * @param messages messages
     * @return ChatCompletionResponseBody
     */
    public ChatCompletionResponseBody askOriginal(String model, String user, List<Message> messages, Integer maxTokens) {
    	
        RequestBody body = RequestBody.create(buildChatRequestBody(model, user, messages, maxTokens), MediaType.get("application/json; charset=utf-8"));
        
        Request.Builder builder = new Request.Builder()
                .url(apiHost)
                .post(body);
        
        addHttpHeaders(builder);
        
        Request request = builder.build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                if (response.body() == null) {
                    log.error("Request failed: {}, please try again", response.message());
                    throw new BizException(response.code(), "Request failed");
                } else {
                	String bodyString = response.body().string();
                    log.error("Request failed: {}, please try again", bodyString);
                    throw new BizException(response.code(), bodyString);
                }
            } else {
                assert response.body() != null;
                String bodyString = response.body().string();
                return objectMapper.readValue(bodyString, ChatCompletionResponseBody.class);
            }
        } catch (IOException e) {
            log.error("Request failed: {}", e.getMessage());
            throw new BizException(Error.SERVER_HAD_AN_ERROR.getCode(), e.getMessage());
        }
    }

    /**
     * ask for response message
     *
     * @param model model
     * @param role role
     * @param content content
     * @return String message
     */
    public String ask(String model, String user, String content, Integer maxTokens) {
        ChatCompletionResponseBody chatCompletionResponseBody = askOriginal(model, user, Collections.singletonList(Message.builder()
                .role("user")
                .content(content)
                .build()),
        		maxTokens);
        List<ChatCompletionResponseBody.Choice> choices = chatCompletionResponseBody.getChoices();
        StringBuilder result = new StringBuilder();
        for (ChatCompletionResponseBody.Choice choice : choices) {
            result.append(choice.getMessage().getContent());
        }
        return result.toString();
    }

	private void addHttpHeaders(Request.Builder builder) {
		if(httpHeaders==null) {
        	builder.addHeader("Authorization", "Bearer " + apiKey);
        } else {
        	for (Map.Entry<String, String> entry : httpHeaders.entrySet()) 
        		builder.addHeader(entry.getKey(), entry.getValue());
        }
	}

}
