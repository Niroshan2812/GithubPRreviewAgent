# GitHub PR-Agent (GeminiAI)

This is a Spring Boot application that acts as an automated code reviewer for your GitHub repositories. It listens for Pull Request (PR) webhooks, analyzes the code changes (the "diff") using the Google Gemini API, and posts the AI-generated review as a comment directly on the PR.

<img width="625" height="557" alt="image" src="https://github.com/user-attachments/assets/4671b633-b51e-402f-bb21-b7de0277fb1a" />




## In here You can 
* Automated Code Reviews  
  - Triggers on Opend or Reopend Pull requessts.
* AI powerd
  - Uses Gemini api for this
* Secure
  - Verifies GitHub webhook signature (HMAC-SHA256) to ensure req from GitHub. 
* Customizable
  - The AI's review prompt is easily editable in src/main/resources/prompts/codeReviewPrompt.txt.
* Modern and reactive
   - Built with Spring Boot and the non-blocking Spring WebClient.
 
## How it work 

1. **User Action**  
   A developer opens or reopens a **Pull Request** in a configured repository.
2. **Webhook Triggered**  
   GitHub sends a `pull_request` **webhook event** to this app’s endpoint: /api/githubWebhook
3. **Security Validation**  
The **WebhookSecurityService** verifies the webhook’s **signature** using your configured secret to ensure authenticity.
4. **Fetch Diff**  
The **GitHubReviewService** retrieves the PR’s **diff** from the GitHub API.

5. **AI Code Review**  
The diff and a custom **prompt** (from `codeReviewPrompt.txt`) are sent to the **Google Gemini API**.  
Gemini then generates an **AI-powered code review** with detailed suggestions.

6. **Post Review**  
Finally, the service **posts the AI-generated review** as a **comment** directly on the Pull Request via the GitHub API.
---
> ✨ This workflow automates code review, ensuring every PR gets instant, intelligent feedback powered by Gemini.


## Try it 💡

### Prerequisites
* Java 21
* Maven
* A GitHub Account (Just ASK😂)  
* A Google Gemini API
* ngrok (or another way to expose your local server to the internet)

### Configuration 

The application uses properties from src/main/resources/application.properties. You will need to provide three secret keys.

 src/main/resources/application.properties

 --- GitHub API ---
 1. Your GitHub Personal Access Token (PAT)
Go to GitHub > Settings > Developer settings > Personal access tokens (Classic) Generate a new token with the `repo` scope (or `public_repo` for public repos).
```
github.api.token=${GITHUB_API_TOKEN}
```

 --- GitHub Webhook ---
 2. Your Webhook Secret
    This can be ANY strong, random string you create (e.g., use a password generator). You will use this SAME string in the GitHub Webhook settings (Step 2.3).
  ```
github.webhook.secret=${GITHUB_WEBHOOK_SECRET}
```

 --- Gemini API ---
 3. Your Google Gemini API Key
    Generate this from Google AI Studio (https://aistudio.google.com/app/apikey)
```
gemini.api.key=${GEMINI_API_KEY}
```

### Run the application 

#### Locally 
1. Run the spring boot
2. Expose your local server: The app runs on port 8080. You need to expose this to the public internet so GitHub can send webhooks to it. ngrok is the easiest way.
```
# In a new terminal
ngrok http 8080
```
ngrok will give you a Forwarding URL, like https://1234abcd.ngrok.io. Copy this URL.

------------------------------------------------- GitHub -------------------------------------------------------------------

You need to go to two different "Settings" pages

      * Your Repository Settings: To create the Webhook so your repository can send events to your application.
      
      * Your Account Developer Settings: To create the Personal Access Token (PAT) so your application can log in to GitHub as you.
      
3. Set Up the GitHub Webhook
  * Go to the GitHub repository you want to monitor.
  * Go to Settings > Webhooks > Add webh
  * Fill out the form
     - Payload URL -  Your ngrok URL (or server URL) + the endpoint. Sample -> https://1234abcd.ngrok.io/api/githubWebhook
     - Content Type - application/json
     - Secret - The exact same github.webhook.secret string you set in your application.properties or environment variables.
     - Which events would you like to trigger this webhook?
         * Select "Let me select individual events."
         * Uncheck "Pushes".
         * Check "Pull requests".
      
4. Click Add requests
5. Personal Access Token (PAT)
   * This token allows your Spring Boot application to authenticate with the GitHub API (to post comments, read the diff, etc.).
  
     1. Click on your profile picture in the top-right corner of GitHub.
     2. Go to Settings.
     3. In the left sidebar, scroll all the way down and click on < > Developer settings.
     4. Click on Personal access tokens > Tokens (classic).
     5. Click "Generate new token" and select "Generate new token (classic)".
     6. Note: Give it a descriptive name (e.g., pr-review-agent).
     7. Expiration: Set this to your preference (e.g., 90 days).
     8. Scopes: Select the repo scope. This is all you need for it to access your repositories and post comments.
     9. Click "Generate token" and copy the token immediately. You will not see it again.
     10. Set this value for github.api.token (or the GITHUB_API_TOKEN environment variable).

That's it! Now, when you open a new PR in that repository, this application will receive the event, and you should see an AI-generated review appear as a comment
   

















