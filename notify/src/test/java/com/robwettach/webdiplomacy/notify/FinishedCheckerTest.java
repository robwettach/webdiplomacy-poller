package com.robwettach.webdiplomacy.notify;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import com.robwettach.webdiplomacy.model.CountryState;
import com.robwettach.webdiplomacy.model.CountryStatus;
import com.robwettach.webdiplomacy.model.GameDate;
import com.robwettach.webdiplomacy.model.GamePhase;
import com.robwettach.webdiplomacy.model.GameState;
import com.robwettach.webdiplomacy.model.Season;
import com.robwettach.webdiplomacy.model.UserInfo;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import one.util.streamex.EntryStream;
import org.junit.jupiter.api.Test;

class FinishedCheckerTest {
    @Test
    void shouldNotNotifyIfNotFinished() {
        Snapshot previous = makeSnapshotWithFinishedAndCountries(false, Map.of());
        Snapshot current = makeSnapshotWithFinishedAndCountries(false, Map.of());

        List<Diff> diffs = new FinishedChecker().check(previous, current);
        assertThat("Unexpected diffs", diffs, is(empty()));
    }

    @Test
    void shouldNotNotifyIfStillFinished() {
        Snapshot previous = makeSnapshotWithFinishedAndCountries(true, Map.of());
        Snapshot current = makeSnapshotWithFinishedAndCountries(true, Map.of());

        List<Diff> diffs = new FinishedChecker().check(previous, current);
        assertThat("Unexpected diffs", diffs, is(empty()));
    }

    @Test
    void shouldNotifyWithWinner() {
        Snapshot previous = makeSnapshotWithFinishedAndCountries(false, Map.of(
                "c1", CountryStatus.Completed,
                "c2", CountryStatus.Ready,
                "c3", CountryStatus.Ready,
                "c4", CountryStatus.Defeated));
        Snapshot current = makeSnapshotWithFinishedAndCountries(true, Map.of(
                "c1", CountryStatus.Won,
                "c2", CountryStatus.Survived,
                "c3", CountryStatus.Survived,
                "c4", CountryStatus.Defeated));

        List<Diff> diffs = new FinishedChecker().check(previous, current);
        assertThat("Unexpected diffs", diffs, hasSize(1));
        assertThat("Unexpected message", diffs.get(0).getMessage(), is("Game Finished - Won by c1"));
    }

    @Test
    void shouldNotifyForDraw() {
        Snapshot previous = makeSnapshotWithFinishedAndCountries(false, Map.of(
                "c1", CountryStatus.Completed,
                "c2", CountryStatus.Ready,
                "c3", CountryStatus.Ready,
                "c4", CountryStatus.Defeated));
        Snapshot current = makeSnapshotWithFinishedAndCountries(true, Map.of(
                "c1", CountryStatus.Drawn,
                "c2", CountryStatus.Drawn,
                "c3", CountryStatus.Drawn,
                "c4", CountryStatus.Defeated));

        List<Diff> diffs = new FinishedChecker().check(previous, current);
        assertThat("Unexpected diffs", diffs, hasSize(1));
        assertThat("Unexpected message", diffs.get(0).getMessage(), is("Game Finished - Drawn by c1, c2, c3"));
    }

    private Snapshot makeSnapshotWithFinishedAndCountries(boolean finished, Map<String, CountryStatus> countries) {
        return Snapshot.create(ZonedDateTime.now(), GameState.builder()
                .name("test")
                .id(1)
                .date(GameDate.create(Season.Autumn, 1913))
                .phase(finished ? GamePhase.Finished : GamePhase.Diplomacy)
                .finished(finished)
                .countries(EntryStream.of(countries)
                        .mapKeyValue((c, s) -> CountryState.builder()
                                .countryName(c)
                                .user(UserInfo.create("test", 1))
                                .supplyCenterCount(0)
                                .unitCount(0)
                                .status(s)
                                .build())
                        .collect(toList()))
                .build());
    }
}
