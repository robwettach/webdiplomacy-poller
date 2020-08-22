package com.robwettach.webdiplomacy.notify;

import com.robwettach.webdiplomacy.model.GameState;
import java.util.List;

/**
 * Simple interface for checking for {@link Diff}s related to the most recent game state.
 *
 * <p><b>Note:</b> Implementations are expected to be <em>stateful</em> in order to detect that the newly-provided
 * {@code state} has changed.
 */
public interface DiffChecker {
    /**
     * Check the new {@link GameState} against previous state to determine if there are any new {@link Diff}s to report.
     *
     * @param state The latest {@link GameState}
     * @return A list of {@link Diff}s between the previous and latest {@code state}.  Not-{@code null}.
     *         If no {@link Diff}s are found, an empty {@link List} is returned.
     */
    List<Diff> check(GameState state);
}
