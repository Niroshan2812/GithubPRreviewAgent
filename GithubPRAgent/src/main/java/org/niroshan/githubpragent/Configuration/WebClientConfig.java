package org.niroshan.githubpragent.Configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    // call API keys
    @Value("${github.api.token}")
    private String GitHubToken;

    @Value("${gemini.api.key}")
    private String LlmApiKey;

    // a webclient bean for taking to GitHub API
    @Bean("githubWebClient")
    public WebClient githubWebClient (){
        return WebClient.builder()
                .baseUrl("https://api.github.com") // Base URL for most GitHub API calls
                .defaultHeader("Authorization", "Bearer " + GitHubToken)
                .defaultHeader("Accept", "application/vnd.github.v3+json")
                .build();
    }

    // A webClient bean for taling to LLM API
    @Bean("llmWebClient")
    public WebClient llmWebClient(){
        return WebClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com") // Example for OpenAI
               // .defaultHeader("Authorization", "Bearer " + LlmApiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
