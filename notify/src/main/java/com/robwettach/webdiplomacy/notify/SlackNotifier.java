package com.robwettach.webdiplomacy.notify;

import static com.robwettach.webdiplomacy.json.Json.OBJECT_MAPPER;
import static java.util.stream.Collectors.joining;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

@AutoValue
public abstract class SlackNotifier implements Notifier {
    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    public abstract String getWebhookUrl();

    public static SlackNotifier create(String webhookUrl) {
        return new AutoValue_SlackNotifier(webhookUrl);
    }

    @Override
    public void notify(List<Diff> diffs) {
        String content = diffs.stream().filter(Diff::isGlobal).map(d -> "- " + d).collect(joining("\n"));
        Map<String, String> body = ImmutableMap.of("content", content);
        String bodyJson;
        try {
            bodyJson = OBJECT_MAPPER.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            System.err.println("Failed to render JSON");
            e.printStackTrace();
            return;
        }
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getWebhookUrl()))
                .POST(HttpRequest.BodyPublishers.ofString(bodyJson))
                .build();
        HttpResponse<String> response;
        try {
            response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.err.println("Failed to POST to Slack");
            e.printStackTrace();
            return;
        }
        if (response.statusCode() != 200) {
            System.err.printf("%d - %s%n", response.statusCode(), response.body());
        }
    }
}
