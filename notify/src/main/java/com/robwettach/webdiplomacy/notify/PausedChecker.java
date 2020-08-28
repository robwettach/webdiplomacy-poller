package com.robwettach.webdiplomacy.notify;

import java.util.Collections;
import java.util.List;

/**
 * {@link DiffChecker} that reports when the game is paused or unpaused.
 */
public class PausedChecker implements DiffChecker {
    @Override
    public List<Diff> check(Snapshot previous, Snapshot current) {
        if (current.getState().isPaused() != previous.getState().isPaused()) {
            if (current.getState().isPaused()) {
                return Collections.singletonList(Diff.global("The game is now paused"));
            } else {
                return Collections.singletonList(Diff.global("The game is now un-paused"));
            }
        } else {
            return Collections.emptyList();
        }
    }
}
