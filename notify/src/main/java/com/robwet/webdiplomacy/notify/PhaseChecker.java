package com.robwettach.webdiplomacy.notify;

import com.robwettach.webdiplomacy.model.DatePhase;
import com.robwettach.webdiplomacy.model.GameState;

import java.util.Collections;
import java.util.List;

public class PhaseChecker implements DiffChecker {
    private DatePhase currentPhase = null;

    @Override
    public List<Diff> check(GameState state) {
        if (!state.getDatePhase().equals(currentPhase)) {
            currentPhase = state.getDatePhase();
            return Collections.singletonList(Diff.global("Moving to: %s", currentPhase));
        } else {
            return Collections.emptyList();
        }
    }
}
