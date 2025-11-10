package org.niroshan.githubpragent.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;

/*
    Use WebClient beans to orchestrate the API call
    Sample Jackson to parse the JSON.
    todo -
    to map the JSON fields  --> Java POJO/Recorde classes
        pullRequestPayload, PREvent
 */

// ------------------- Assumption -----------------------
// have a simple record to hold the LLM request
record  LlmRequest (String model, String prompt){}
// have a simple record to hold the GitHub comment
record GitHubComment (String body){}
// =================End Assumption =========================


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

    // main logic for process the PR

    private void processPullRequest(String payload){
        try{
            // parse payload to get essential info
            var jsonNode = objectMapper.readTree(payload);

            String action = jsonNode.path("action").asText();

            // ONLY ACT on "opened or "reopened" events"
            if(! "opened".equals(action) && !"reopened".equals(action)){
                System.out.println("Ignore action: " + action);
                return;
            }
            //get the URLs we need from the payload
            String diffURL = jsonNode.path("pull_Request").path("diff_url").asText();
            String commentsURL  = jsonNode.path("pull_Request").path("comments_url").asText();

            // fetch the diff from GITHUB
            String diffContent = githubClient.get()
                    .uri(diffURL)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            // get review from LLM
            String review = getReviewFromLLM(diffContent);

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
            ClassPathResource resource =  new ClassPathResource("prompts"+fileName);
            return Files.readString(Path.of(resource.getURI()));
        }catch (Exception e){
            throw new RuntimeException("Failed to load prompt template: "+fileName, e);
        }
    }

    // call LLM API to get a code review
    private String getReviewFromLLM(String diff){
        String prompt = loadPromptTemplate("codeReviewPrompt.txt" + diff);

        // create the request body
        LlmRequest request = new LlmRequest("gpt-4o-mini", prompt);

        //call the LLM API
        String llmResponse = llmClient.post()
                .uri("chat/completions")
                .bodyValue(llmRequest)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        // parse the LLM's json response

        return "Parsed LLM review from: " + llmResponse.substring(0, 50);
    }
}
