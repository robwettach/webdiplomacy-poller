package com.robwettach.webdiplomacy.notify;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableList;
import com.robwettach.webdiplomacy.json.Json;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class CompositeNotifierTest {
    @Test
    void shouldSerializeAsUnion() throws JsonProcessingException {
        String json = Json.OBJECT_MAPPER.writeValueAsString(CompositeNotifier.create(StdOutNotifier.create()));
        assertThat("Unexpected JSON representation", json, is("{\"notifiers\":[{\"stdOut\":{}}]}"));
    }
    @Test
    void shouldDeserializeUnion() throws JsonProcessingException {
        String json = "{\"notifiers\":[{\"stdOut\":{}}]}";
        CompositeNotifier composite = Json.OBJECT_MAPPER.readValue(json, CompositeNotifier.class);
        assertThat("Unexpected JSON representation", composite, is(CompositeNotifier.create(StdOutNotifier.create())));
    }

    @Test
    void shouldRoundTripToJson() throws JsonProcessingException {
        CompositeNotifier expected = CompositeNotifier.create(StdOutNotifier.create());
        String json = Json.OBJECT_MAPPER.writeValueAsString(expected);
        CompositeNotifier actual = Json.OBJECT_MAPPER.readValue(json, CompositeNotifier.class);
        assertThat("Unexpected round-trip value", actual, is(equalTo(expected)));
    }

    @Test
    void shouldRoundTripToJsonWithMultipleNotifiers() throws JsonProcessingException {
        CompositeNotifier expected = CompositeNotifier.create(StdOutNotifier.create(), SlackNotifier.create("url"));
        String json = Json.OBJECT_MAPPER.writeValueAsString(expected);
        CompositeNotifier actual = Json.OBJECT_MAPPER.readValue(json, CompositeNotifier.class);
        assertThat("Unexpected round-trip value", actual, is(equalTo(expected)));
    }

    @Test
    void shouldAcceptZeroNotifiers() {
        CompositeNotifier c = CompositeNotifier.create();
        c.notify(Collections.emptyList());
    }

    @Test
    void shouldNotifyOnce() {
        Notifier n = mock(Notifier.class);
        List<Diff> ds = Collections.emptyList();
        CompositeNotifier.create(n).notify(ds);
        verify(n).notify(ds);
    }

    @Test
    void shouldNotifyMultipleTimes() {
        List<Notifier> ns = ImmutableList.of(
                mock(Notifier.class),
                mock(Notifier.class),
                mock(Notifier.class));

        List<Diff> ds = Collections.emptyList();
        CompositeNotifier.create(ns).notify(ds);

        ns.forEach(n -> verify(n).notify(ds));
    }
}
