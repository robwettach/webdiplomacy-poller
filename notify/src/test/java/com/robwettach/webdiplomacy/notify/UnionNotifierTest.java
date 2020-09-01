package com.robwettach.webdiplomacy.notify;

import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.robwettach.webdiplomacy.json.Json;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class UnionNotifierTest {
    @Test
    void shouldThrowForMultipleComponentsSet() {
        UnionNotifier.Builder builder = UnionNotifier.builder()
                .composite(CompositeNotifier.create())
                .slack(SlackNotifier.create(""))
                .stdOut(StdOutNotifier.create());
        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void shouldThrowForNoComponentsSet() {
        assertThrows(IllegalStateException.class, () -> UnionNotifier.builder().build());
    }

    @Test
    void shouldThrowForUnknownNotifierType() {
        assertThrows(IllegalArgumentException.class, () -> UnionNotifier.create(diffs -> {}));
    }

    static Stream<Arguments> shouldAcceptAllSupportedNotifiers() {
        return Stream.of(
                StdOutNotifier.create(),
                SlackNotifier.create(""),
                CompositeNotifier.create())
                .map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource
    void shouldAcceptAllSupportedNotifiers(Notifier notifier) {
        UnionNotifier union = UnionNotifier.create(notifier);
        Notifier component = union.getNotifier();
        assertThat("Unexpected Notifier", component, is(sameInstance(notifier)));
    }

    @Test
    void shouldSerializeStdOutToJson() throws JsonProcessingException {
        String json = Json.OBJECT_MAPPER.writeValueAsString(UnionNotifier.create(StdOutNotifier.create()));
        assertThat("Unexpected JSON representation", json, is("{\"stdOut\":{}}"));
    }

    @Test
    void shouldDeserializeStdOut() throws JsonProcessingException {
        String json = "{\"stdOut\":{}}";
        UnionNotifier union = Json.OBJECT_MAPPER.readValue(json, UnionNotifier.class);
        assertThat(
                "Missing StdOutNotifier",
                union.getStdOut(),
                is(optionalWithValue(equalTo(StdOutNotifier.create()))));
    }

    @Test
    void shouldSerializeSlackToJson() throws JsonProcessingException {
        String json = Json.OBJECT_MAPPER.writeValueAsString(UnionNotifier.create(SlackNotifier.create("url")));
        assertThat("Unexpected JSON representation", json, is("{\"slack\":{\"webhookUrl\":\"url\"}}"));
    }

    @Test
    void shouldDeserializeSlack() throws JsonProcessingException {
        String json = "{\"slack\":{\"webhookUrl\":\"url\"}}";
        UnionNotifier union = Json.OBJECT_MAPPER.readValue(json, UnionNotifier.class);
        assertThat(
                "Missing SlackNotifier",
                union.getSlack(),
                is(optionalWithValue(equalTo(SlackNotifier.create("url")))));
    }

    @Test
    void shouldSerializeCompositeToJson() throws JsonProcessingException {
        String json = Json.OBJECT_MAPPER.writeValueAsString(UnionNotifier.create(CompositeNotifier.create()));
        assertThat("Unexpected JSON representation", json, is("{\"composite\":{\"notifiers\":[]}}"));
    }

    @Test
    void shouldDeserializeComposite() throws JsonProcessingException {
        String json = "{\"composite\":{\"notifiers\":[]}}";
        UnionNotifier union = Json.OBJECT_MAPPER.readValue(json, UnionNotifier.class);
        assertThat(
                "Missing CompositeNotifier",
                union.getComposite(),
                is(optionalWithValue(equalTo(CompositeNotifier.create()))));
    }
}
