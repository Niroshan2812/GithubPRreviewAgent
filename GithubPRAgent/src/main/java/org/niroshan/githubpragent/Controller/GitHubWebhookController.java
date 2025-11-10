package org.niroshan.githubpragent.Controller;

import org.niroshan.githubpragent.Service.GitHubReviewSearvice;
import org.niroshan.githubpragent.Service.WebhookSecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GitHubWebhookController {

    @Autowired
    private GitHubReviewSearvice reviewSearvice;

    @Autowired
    private WebhookSecurityService webhookSecurityService;

    @PostMapping("/api/githubWebhook")
    public void handleGithubWebhook(@RequestBody String payload,
                                    @RequestHeader("X-Hub-Signature-256")String signature) {

        System.out.println("raw payload recived : "+ payload);
        System.out.println("signature recived : "+ signature);
      // validate signature
        if(!webhookSecurityService.isValidSignature(payload, signature)) {
            System.out.println("Invalid signature");
            return;
        }
        //Always validate payload to the searvice layer for processing
        reviewSearvice.processPullRequest(payload);

    }
}
