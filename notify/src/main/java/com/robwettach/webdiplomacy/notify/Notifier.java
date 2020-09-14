package com.robwettach.webdiplomacy.notify;

import com.robwettach.webdiplomacy.diff.Diff;
import java.util.List;

/**
 * Simple interface for sending notifications about {@link Diff}s.
 */
public interface Notifier {
    /**
     * Send a notification for the provided {@code diffs}.
     *
     * @param diffs The {@link Diff}s to notify about
     */
    void notify(List<Diff> diffs);
}
