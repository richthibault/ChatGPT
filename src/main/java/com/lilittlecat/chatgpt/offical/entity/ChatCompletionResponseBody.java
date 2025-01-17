package com.lilittlecat.chatgpt.offical.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 * Response body for ChatGPT API.
 * </p>
 * see: <a href="https://platform.openai.com/docs/api-reference/chat">https://platform.openai.com/docs/api-reference/chat</a>
 *
 * @author <a href="https://github.com/LiLittleCat">LiLittleCat</a>
 * @since 2023/3/2
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatCompletionResponseBody {

    @JsonProperty(value = "id")
    public String id;
    @JsonProperty(value = "object")
    public String object;
    @JsonProperty(value = "created")
    public Long created;
    @JsonProperty(value = "model")
    public String model;
    @JsonProperty(value = "choices")
    public List<Choice> choices;
    @JsonProperty(value = "usage")
    public Usage usage;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Choice {
        @JsonProperty(value = "index")
        public Integer index;
        @JsonProperty(value = "message")
        public Message message;
        @JsonProperty(value = "finish_reason")
        public String finishReason;
    }


    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Usage {
        @JsonProperty(value = "prompt_tokens")
        public Integer promptTokens;
        @JsonProperty(value = "completion_tokens")
        public Integer completionTokens;
        @JsonProperty(value = "total_tokens")
        public Integer totalTokens;
    }
}
