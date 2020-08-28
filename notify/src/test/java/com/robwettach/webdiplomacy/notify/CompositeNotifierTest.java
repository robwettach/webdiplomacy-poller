package com.robwettach.webdiplomacy.notify;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class CompositeNotifierTest {
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
