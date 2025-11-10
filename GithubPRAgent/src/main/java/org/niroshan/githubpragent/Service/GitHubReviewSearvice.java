package org.niroshan.githubpragent.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.util.List;

/*
    Use WebClient beans to orchestrate the API call
    Sample Jackson to parse the JSON.
    todo -
    to map the JSON fields  --> Java POJO/Recorde classes
        pullRequestPayload, PREvent
 */
// -------------------------Gemini request record ---------
record GeminiRequest(List<Content> contents) {}
record Content(List<Part> parts) {}
record Part(String text) {}

//-----------------------Gemini respond record -----------
@JsonIgnoreProperties(ignoreUnknown = true)
record GeminiResponse(List<Candidate> candidates) {}

@JsonIgnoreProperties(ignoreUnknown = true)
record Candidate(Content content) {}

// --------------------github comment record -----------
record GitHubComment (String body){}



@Service
public class GitHubReviewSearvice {
    @Autowired
    @Qualifier("githubWebClient")
    private WebClient githubClient;

    @Autowired
    @Qualifier("llmWebClient")
    private WebClient llmClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${gemini.api.key}")
    private String geminiApiKey;
    // main logic for process the PR

    public void processPullRequest(String payload){
        try{
            // parse payload to get essential info
            var jsonNode = objectMapper.readTree(payload);

            String action = jsonNode.path("action").asText();
           // System.out.println("Action field: "+jsonNode.path("action").asText());

            // ONLY ACT on "opened or "reopened" events"
            if(!"opened".equals(action) && !"reopened".equals(action)){
                System.out.println("Ignore action: " + action);
                return;
            }
            //get the URLs we need from the payload
            String prApiUrl = jsonNode.path("pull_request").path("url").asText();
            String commentsURL  = jsonNode.path("pull_request").path("comments_url").asText();

            // fetch the diff from GITHUB
            String diffContent = githubClient.get()
                    .uri(prApiUrl)
                    .header("Accept", "application/vnd.github.v3.diff")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            // get review from LLM
            String review = getReviewFromLLM(diffContent);

            /*
            System.out.println("Review Show case: "+review );
             System.out.println("Payload  Show case: "+ payload);
            System.out.println("Pull request diff_url: " + jsonNode.path("pull_request").path("diff_url"));
             */

            // post the review back to GitHub
            GitHubComment comment = new GitHubComment("AI Review "+ review);

            githubClient.post()
                    .uri(commentsURL)
                    .bodyValue(comment)
                    .retrieve()
                    .toBodilessEntity()
                    .block();

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    // call prompt class to get prompts
    private String loadPromptTemplate (String fileName){
        try{
            ClassPathResource resource =  new ClassPathResource("prompts/"+fileName);
            return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        }catch (Exception e){
            throw new RuntimeException("Failed to load prompt template: "+fileName, e);
        }
    }

    // call LLM API to get a code review
    private String getReviewFromLLM(String diff){
      //  System.out.println("Gemini api Key " + geminiApiKey);


        String prompt = loadPromptTemplate("codeReviewPrompt.txt");

        String finalPrompt = prompt + "\n\nHere is the code diff to review:\n\n" + diff;
        /*
         //call the LLM API
        String llmResponse = llmClient.post()
                .uri("chat/completions")
                .bodyValue(llmRequest)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        // parse the LLM's json response
        System.out.println( "LLM RESPOND " +llmResponse);

        return "Parsed LLM review from: " + llmResponse.substring(0, 50);
         */

        // create gemini specific req body
        Part part = new Part(finalPrompt);
        Content content = new Content(List.of(part));
        GeminiRequest geminiRequest = new GeminiRequest(List.of(content));

        // Call the Gemini API
        // the model is gemini 1.5 flash
        GeminiResponse llmRespond = llmClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1beta/models/gemini-2.5-flash:generateContent")
                        .queryParam("key",geminiApiKey)
                        .build())
                .bodyValue(geminiRequest)
                .retrieve()
                .bodyToMono(GeminiResponse.class)
                .block();

        try{
            String reviewText = llmRespond.candidates().get(0).content().parts().get(0).text();
            //System.out.println("Review Show case: "+reviewText);
            return reviewText;
        }catch (Exception e){
            e.printStackTrace();
            return "Could not get review";
        }

    }
}
