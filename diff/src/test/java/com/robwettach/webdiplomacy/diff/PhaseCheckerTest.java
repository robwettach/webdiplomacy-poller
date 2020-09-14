package com.robwettach.webdiplomacy.diff;

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
import org.junit.jupiter.api.Test;

class PhaseCheckerTest {
    @Test
    void shouldNotNotifyOnSamePhase() {
        Snapshot previous = makeSnapshotWithDatePhase(Season.Spring, 1901, GamePhase.Diplomacy);
        Snapshot current = makeSnapshotWithDatePhase(Season.Spring, 1901, GamePhase.Diplomacy);

        List<Diff> diffs = new PhaseChecker().check(previous, current);
        assertThat("Unexpected diffs", diffs, is(empty()));
    }

    @Test
    void shouldNotifyOnChangingPhase() {
        Snapshot previous = makeSnapshotWithDatePhase(Season.Spring, 1901, GamePhase.Diplomacy);
        Snapshot current = makeSnapshotWithDatePhase(Season.Spring, 1901, GamePhase.Retreats);

        List<Diff> diffs = new PhaseChecker().check(previous, current);
        assertThat("Unexpected diffs", diffs, hasSize(1));
        assertThat("Unexpected message", diffs.get(0).getMessage(), is("Moving to: Spring, 1901, Retreats"));
    }

    @Test
    void shouldNotNotifyOnFinished() {
        Snapshot previous = makeSnapshotWithDatePhase(Season.Autumn, 1914, GamePhase.Retreats);
        Snapshot current = makeSnapshotWithDatePhase(Season.Autumn, 1914, GamePhase.Finished);

        List<Diff> diffs = new PhaseChecker().check(previous, current);
        assertThat("Unexpected diffs", diffs, is(empty()));
    }

    private Snapshot makeSnapshotWithDatePhase(Season season, int year, GamePhase phase) {
        return Snapshot.create(ZonedDateTime.now(), GameState.builder()
                .name("test")
                .id(1)
                .date(GameDate.create(season, year))
                .phase(phase)
                .build());
    }
}
