package com.robwettach.webdiplomacy.notify;

import com.robwettach.webdiplomacy.model.DatePhase;
import com.robwettach.webdiplomacy.model.GameState;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

/**
 * {@link DiffChecker} that reports when there is an hour or less remaining before the next turn.
 */
public class HourRemainingChecker implements DiffChecker {
    private DatePhase hourRemainingNotified = null;

    @Override
    public List<Diff> check(GameState state) {
        DatePhase currentPhase = state.getDatePhase();

        if (!currentPhase.equals(hourRemainingNotified)
                && state.getNextTurnAt()
                .map(d -> d.minusHours(1).isBefore(ZonedDateTime.now(ZoneOffset.UTC)))
                .orElse(false)) {
            hourRemainingNotified = currentPhase;
            return Collections.singletonList(Diff.global("One more hour to submit moves!"));
        } else {
            return Collections.emptyList();
        }
    }
}
