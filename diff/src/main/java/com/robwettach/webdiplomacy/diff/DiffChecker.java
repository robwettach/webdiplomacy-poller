package com.robwettach.webdiplomacy.diff;

import java.util.List;

/**
 * Simple interface for checking for {@link Diff}s related to the most recent game state.
 *
 * <p><b>Note:</b> Implementations are expected to be <em>stateless</em>, only detecting differences between the
 * {@code previous} and {@code current} {@link Snapshot}s.
 */
public interface DiffChecker {
    /**
     * Check the latest {@link Snapshot} against the previous to determine if there are any new {@link Diff}s to report.
     *
     * @param previous The previous {@link Snapshot}
     * @param current The latest {@link Snapshot}
     * @return A list of {@link Diff}s between the previous and latest {@code state}.  Not-{@code null}.
     *         If no {@link Diff}s are found, an empty {@link List} is returned.
     */
    List<Diff> check(Snapshot previous, Snapshot current);
}
