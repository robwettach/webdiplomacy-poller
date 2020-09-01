package com.robwettach.webdiplomacy.notify;

import static com.robwettach.webdiplomacy.json.Json.OBJECT_MAPPER;
import static java.util.stream.Collectors.joining;

import com.fasterxml.jackson.annotation.JsonCreator;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * {@link Notifier} that sends notifications to a Slack webhook, configured as per
 * <a href="https://github.com/robwettach/webdiplomacy-poller#slack-integration">the README</a>.
 */
@AutoValue
public abstract class SlackNotifier implements Notifier {
    private static final Logger LOG = LogManager.getLogger(SlackNotifier.class);
    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    public abstract String getWebhookUrl();

    @JsonCreator
    public static SlackNotifier create(String webhookUrl) {
        return new AutoValue_SlackNotifier(webhookUrl);
    }

    @Override
    public void notify(List<Diff> diffs) {
        LOG.info("Sending {} diffs to Slack", diffs.size());
        String content = diffs.stream().filter(Diff::isGlobal).map(d -> "- " + d).collect(joining("\n"));
        Map<String, String> body = ImmutableMap.of("content", content);
        String bodyJson;
        try {
            bodyJson = OBJECT_MAPPER.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            LOG.error("Failed to render JSON", e);
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
            LOG.error("Failed to POST to Slack", e);
            return;
        }
        if (response.statusCode() != 200) {
            LOG.warn("Non-OK status received from Slack: {} - {}", response.statusCode(), response.body());
        }
    }
}
