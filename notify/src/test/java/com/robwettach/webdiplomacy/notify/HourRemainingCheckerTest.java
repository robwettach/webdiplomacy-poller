package com.robwettach.webdiplomacy.notify;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import com.robwettach.webdiplomacy.model.GameDate;
import com.robwettach.webdiplomacy.model.GamePhase;
import com.robwettach.webdiplomacy.model.GameState;
import com.robwettach.webdiplomacy.model.Season;
import java.time.ZonedDateTime;
import java.util.List;
import javax.annotation.Nullable;
import org.junit.jupiter.api.Test;

class HourRemainingCheckerTest {
    @Test
    void shouldNotNotifyWhenPaused() {
        Snapshot previous = makeSnapshotWithNextTurnAt(ZonedDateTime.now(), ZonedDateTime.now());
        Snapshot current = makeSnapshotWithNextTurnAt(ZonedDateTime.now(), null);

        List<Diff> diffs = new HourRemainingChecker().check(previous, current);
        assertThat("Unexpected diffs", diffs, is(empty()));
    }

    @Test
    void shouldNotNotifyBeforeOneHour() {
        ZonedDateTime nextTurn = ZonedDateTime.now();
        Snapshot previous = makeSnapshotWithNextTurnAt(nextTurn.minusHours(3), nextTurn);
        Snapshot current = makeSnapshotWithNextTurnAt(nextTurn.minusHours(2), nextTurn);

        List<Diff> diffs = new HourRemainingChecker().check(previous, current);
        assertThat("Unexpected diffs", diffs, is(empty()));
    }

    @Test
    void shouldNotNotifyAfterOneHour() {
        ZonedDateTime nextTurn = ZonedDateTime.now();
        Snapshot previous = makeSnapshotWithNextTurnAt(nextTurn.minusMinutes(59), nextTurn);
        Snapshot current = makeSnapshotWithNextTurnAt(nextTurn.minusMinutes(58), nextTurn);

        List<Diff> diffs = new HourRemainingChecker().check(previous, current);
        assertThat("Unexpected diffs", diffs, is(empty()));
    }

    @Test
    void shouldNotifyCrossingOneHour() {
        ZonedDateTime nextTurn = ZonedDateTime.now();
        Snapshot previous = makeSnapshotWithNextTurnAt(nextTurn.minusHours(1).minusNanos(1), nextTurn);
        Snapshot current = makeSnapshotWithNextTurnAt(nextTurn.minusHours(1).plusNanos(1), nextTurn);

        List<Diff> diffs = new HourRemainingChecker().check(previous, current);
        assertThat("Unexpected diffs", diffs, hasSize(1));
        assertThat("Unexpected message", diffs.get(0).getMessage(), is("One more hour to submit moves!"));

    }

    private Snapshot makeSnapshotWithNextTurnAt(ZonedDateTime snapshotTime, @Nullable ZonedDateTime nextTurnTime) {
        return Snapshot.create(snapshotTime, GameState.builder()
                .name("test")
                .id(1)
                .date(GameDate.create(Season.Spring, 1901))
                .phase(GamePhase.Diplomacy)
                .nextTurnAt(nextTurnTime)
                .build());
    }
}
