package com.robwettach.webdiplomacy.diff;

import com.robwettach.webdiplomacy.model.DatePhase;
import com.robwettach.webdiplomacy.model.GamePhase;
import java.util.Collections;
import java.util.List;

/**
 * {@link DiffChecker} that reports when the game proceeds to a new phase.
 */
public class PhaseChecker implements DiffChecker {
    @Override
    public List<Diff> check(Snapshot previous, Snapshot current) {
        DatePhase currentPhase = current.getState().getDatePhase();
        if (!currentPhase.equals(previous.getState().getDatePhase())
                // FinishedChecker will handle this case
                && !currentPhase.getPhase().equals(GamePhase.Finished)) {
            return Collections.singletonList(Diff.global("Moving to: %s", currentPhase));
        } else {
            return Collections.emptyList();
        }
    }
}
