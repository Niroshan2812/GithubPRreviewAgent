package org.niroshan.geminiapitest;

import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Map;

public class GeminiApiTester {
    private static final String API_KEY ="AIzaSyClTZNGB5fQPekBTidJkMZyOr2kdOhK8D8";

    public static void main(String[] args) {
        WebClient webClient = WebClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com")
                .defaultHeader("Content-Type", "application/json")
                .build();

        Map<String, Object> part = Map.of("text", "Hello, what is your name?");
        Map<String, Object> content = Map.of("parts", List.of(part));
        Map<String, Object> requestBody = Map.of("contents", List.of(content));

        System.out.println("Sending Req to Gemini API");

        try{
            String responce = webClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1beta/models/gemini-2.5-flash:generateContent")
                            .queryParam("key", API_KEY)
                            .build())
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            // 4. Print the successful response
            System.out.println("--- SUCCESS! ---");
            System.out.println("API Response:");
            System.out.println(responce);
        }catch (WebClientResponseException e){
            System.err.println("--- ERROR ---");
            System.err.println("HTTP Status: " + e.getRawStatusCode());
            System.err.println("Error Body: " + e.getResponseBodyAsString());
            System.err.println("\nFull Stacktrace:");
            e.printStackTrace();

            if(e.getRawStatusCode() == 404){
                System.err.println("\n--- ACTION REQUIRED ---");
                System.err.println("Got a 404! This almost always means you need to:");
                System.err.println("1. Go to your Google Cloud Console");
                System.err.println("2. Select your project");
                System.err.println("3. Go to 'APIs & Services' > 'Library'");
                System.err.println("4. Search for 'Gemini API' and ENABLE it.");
            }
        }catch (Exception e){
            System.err.println("--- ERROR ---");
            e.printStackTrace();
        }
    }
}
