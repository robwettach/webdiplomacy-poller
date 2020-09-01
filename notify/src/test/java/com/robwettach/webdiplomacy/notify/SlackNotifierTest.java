package com.robwettach.webdiplomacy.notify;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.robwettach.webdiplomacy.json.Json;
import org.junit.jupiter.api.Test;

class SlackNotifierTest {
    @Test
    void shouldSerializeToJson() throws JsonProcessingException {
        String json = Json.OBJECT_MAPPER.writeValueAsString(SlackNotifier.create("url"));
        assertThat("Unexpected JSON representation", json, is("{\"webhookUrl\":\"url\"}"));
    }

    @Test
    void shouldDeserializeFromJson() throws JsonProcessingException {
        SlackNotifier slack = Json.OBJECT_MAPPER.readValue("{\"webhookUrl\":\"url\"}", SlackNotifier.class);
        assertThat("Unexpected notifier", slack, is(SlackNotifier.create("url")));
    }

    @Test
    void shouldRoundTripToJson() throws JsonProcessingException {
        SlackNotifier expected = SlackNotifier.create("url");
        String json = Json.OBJECT_MAPPER.writeValueAsString(expected);
        SlackNotifier actual = Json.OBJECT_MAPPER.readValue(json, SlackNotifier.class);
        assertThat("Unexpected round-trip value", actual, is(equalTo(expected)));
    }
}
