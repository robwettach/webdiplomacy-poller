package com.robwettach.webdiplomacy.notify;

import com.robwettach.webdiplomacy.model.GameState;

import java.util.Collections;
import java.util.List;

public class PausedChecker implements DiffChecker {
    private boolean paused;

    @Override
    public List<Diff> check(GameState state) {
        if (state.isPaused() != paused) {
            paused = state.isPaused();
            if (state.isPaused()) {
                return Collections.singletonList(Diff.global("The game is now paused"));
            } else {
                return Collections.singletonList(Diff.global("The game is now un-paused"));
            }
        } else {
            return Collections.emptyList();
        }
    }
}
