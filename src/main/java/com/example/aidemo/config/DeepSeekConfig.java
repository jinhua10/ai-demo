package com.example.aidemo.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

@Configuration
public class DeepSeekConfig {

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @Value("${spring.ai.openai.base-url}")
    private String baseUrl;

    @Bean
    public OpenAiApi openAiApi() {
        // 使用RestClient自定义配置，确保正确连接DeepSeek API
        RestClient restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .build();

        return new OpenAiApi(baseUrl, apiKey) {
            public org.springframework.http.ResponseEntity<String> chatCompletionEntity(String requestBody) {
                // DeepSeek API使用不同的端点路径
                return restClient.post()
                        .uri("/chat/completions")
                        .body(requestBody)
                        .retrieve()
                        .toEntity(String.class);
            }
        };
    }

    @Bean
    public OpenAiChatModel openAiChatModel(OpenAiApi openAiApi) {
        return new OpenAiChatModel(openAiApi,
                OpenAiChatOptions.builder()
                        .withModel("deepseek-chat")
                        .withTemperature(0.7f)
                        .build());
    }

    @Bean
    public ChatClient chatClient(OpenAiChatModel chatModel) {
        return ChatClient.builder(chatModel).build();
    }
}