package com.robwettach.webdiplomacy.notify;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * {@link Notifier} that sends notifications to standard output.
 */
public class StdOutNotifier implements Notifier {
    private StdOutNotifier() {

    }

    public static StdOutNotifier create() {
        return new StdOutNotifier();
    }

    @Override
    public void notify(List<Diff> diffs) {
        if (!diffs.isEmpty()) {
            System.out.println();
            System.out.println(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
            diffs.forEach(d -> System.out.printf("- %s%n", d));
        }
    }

    @Override
    public int hashCode() {
        return "StdOutNotifier".hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof StdOutNotifier;
    }
}
