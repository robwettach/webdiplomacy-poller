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

class PausedCheckerTest {
    @Test
    void shouldNotNotifyWhenRemainingUnpaused() {
        List<Diff> diffs = new PausedChecker().check(makeSnapshotWithPaused(false), makeSnapshotWithPaused(false));
        assertThat("Unexpected diffs", diffs, is(empty()));
    }

    @Test
    void shouldNotNotifyWhenRemainingPaused() {
        List<Diff> diffs = new PausedChecker().check(makeSnapshotWithPaused(true), makeSnapshotWithPaused(true));
        assertThat("Unexpected diffs", diffs, is(empty()));
    }

    @Test
    void shouldNotifyWhenPausing() {
        List<Diff> diffs = new PausedChecker().check(makeSnapshotWithPaused(false), makeSnapshotWithPaused(true));
        assertThat("Unexpected diffs", diffs, hasSize(1));
        assertThat("Unexpected message", diffs.get(0).getMessage(), is("The game is now paused"));
    }

    @Test
    void shouldNotifyWhenUnpausing() {
        List<Diff> diffs = new PausedChecker().check(makeSnapshotWithPaused(true), makeSnapshotWithPaused(false));
        assertThat("Unexpected diffs", diffs, hasSize(1));
        assertThat("Unexpected message", diffs.get(0).getMessage(), is("The game is now un-paused"));
    }

    private Snapshot makeSnapshotWithPaused(boolean paused) {
        return Snapshot.create(ZonedDateTime.now(), GameState.builder()
                .name("test")
                .id(1)
                .date(GameDate.create(Season.Spring, 1901))
                .phase(GamePhase.Diplomacy)
                .paused(paused)
                .build());
    }
}
