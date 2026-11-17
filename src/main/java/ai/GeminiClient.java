package ai;

import java.io.FileInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Properties;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class GeminiClient {

    private final String apiKey;
    private final String model;
    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient http = HttpClient.newHttpClient();

    // Reads the key and model name from local.properties
    public GeminiClient() throws Exception {
        Properties props = new Properties();
        props.load(new FileInputStream("local.properties"));
        apiKey = props.getProperty("GEMINI_API_KEY");
        model = props.getProperty("GEMINI_MODEL");
    }

    // Sends a question to the model and returns its answer.
    // systemRule = standing rules for the model (use null for none).
    // temperature = 0.0 is steady, higher numbers are more random.
    public String ask(String prompt, String systemRule, double temperature) throws Exception {
        ObjectNode body = mapper.createObjectNode();
        body.putArray("contents").addObject()
            .putArray("parts").addObject().put("text", prompt);

        if (systemRule != null) {
            body.putObject("systemInstruction")
                .putArray("parts").addObject().put("text", systemRule);
        }

        ObjectNode config = body.putObject("generationConfig");
        config.put("temperature", temperature);
        config.put("maxOutputTokens", 2000);

        JsonNode reply = send(model + ":generateContent", body);

     // The answer can come in several pieces, so join them all
     StringBuilder answer = new StringBuilder();
     for (JsonNode part : reply.path("candidates").path(0).path("content").path("parts")) {
         answer.append(part.path("text").asText(""));
     }

     // If the model stopped for a reason other than finishing, show why
     String finishReason = reply.path("candidates").path(0).path("finishReason").asText("");
     if (!finishReason.equals("STOP")) {
         answer.append(" [stopped early: ").append(finishReason).append("]");
     }

     return answer.length() == 0 ? "(empty answer)" : answer.toString();
    }

    // Counts how many tokens a text uses
    public int countTokens(String text) throws Exception {
        ObjectNode body = mapper.createObjectNode();
        body.putArray("contents").addObject()
            .putArray("parts").addObject().put("text", text);
        return send(model + ":countTokens", body).path("totalTokens").asInt();
    }

    // Sends the request over the internet.
    // If the server is busy (429 or 503) or has a temporary problem (500), wait and try again.
    private JsonNode send(String endpoint, ObjectNode body) throws Exception {
        int maxAttempts = 5;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            HttpRequest request = HttpRequest.newBuilder(URI.create(
                        "https://generativelanguage.googleapis.com/v1beta/models/" + endpoint))
                    .header("Content-Type", "application/json")
                    .header("x-goog-api-key", apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                    .build();

            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();

            if (status == 200) {
                return mapper.readTree(response.body());
            }

            boolean temporary = (status == 429 || status == 500 || status == 503);
            if (temporary && attempt < maxAttempts) {
                int waitSeconds = 5 * attempt;
                System.out.println("[Server busy (status " + status + "), waiting "
                        + waitSeconds + " seconds, attempt " + attempt + " of " + maxAttempts + "]");
                Thread.sleep(waitSeconds * 1000L);
                continue;
            }
            throw new RuntimeException("API error " + status + ": " + response.body());
        }
        throw new RuntimeException("Request failed");
    }
}