package org.niroshan.githubpragent.Controller;

import org.niroshan.githubpragent.Service.GitHubReviewSearvice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GitHubWebhookController {

    @Autowired
    private GitHubReviewSearvice reviewSearvice;
}
