package com.robwettach.webdiplomacy.diff;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
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

class DefeatedCheckerTest {
    @Test
    void shouldNotReportAnyCountriesDefeated() {
        Snapshot previous = makeSnapshotWithCountryStatus(Map.of(
                "c1", CountryStatus.NotReceived,
                "c2", CountryStatus.NotReceived,
                "c3", CountryStatus.NotReceived));
        Snapshot current = makeSnapshotWithCountryStatus(Map.of(
                "c1", CountryStatus.NotReceived,
                "c2", CountryStatus.Completed,
                "c3", CountryStatus.Ready));

        List<Diff> diffs = new DefeatedChecker().check(previous, current);
        assertThat("Unexpected diffs", diffs, is(empty()));
    }

    @Test
    void shouldNotReportPreviouslyDefeatedCountry() {
        Snapshot previous = makeSnapshotWithCountryStatus(Map.of(
                "c1", CountryStatus.Defeated,
                "c2", CountryStatus.NotReceived,
                "c3", CountryStatus.NotReceived));
        Snapshot current = makeSnapshotWithCountryStatus(Map.of(
                "c1", CountryStatus.Defeated,
                "c2", CountryStatus.Completed,
                "c3", CountryStatus.Ready));

        List<Diff> diffs = new DefeatedChecker().check(previous, current);
        assertThat("Unexpected diffs", diffs, is(empty()));
    }

    @Test
    void shouldReportNewlyDefeatedCountry() {
        Snapshot previous = makeSnapshotWithCountryStatus(Map.of(
                "c1", CountryStatus.NotReceived,
                "c2", CountryStatus.NotReceived,
                "c3", CountryStatus.NotReceived));
        Snapshot current = makeSnapshotWithCountryStatus(Map.of(
                "c1", CountryStatus.Defeated,
                "c2", CountryStatus.Completed,
                "c3", CountryStatus.Ready));

        List<Diff> diffs = new DefeatedChecker().check(previous, current);
        assertThat("Unexpected diffs", diffs, hasSize(1));
        assertThat("Unexpected message", diffs.get(0).getMessage(), is("c1 has been defeated"));
    }

    @Test
    void shouldReportMultipleNewlyDefeatedCountries() {
        Snapshot previous = makeSnapshotWithCountryStatus(Map.of(
                "c1", CountryStatus.NotReceived,
                "c2", CountryStatus.NotReceived,
                "c3", CountryStatus.NotReceived));
        Snapshot current = makeSnapshotWithCountryStatus(Map.of(
                "c1", CountryStatus.Defeated,
                "c2", CountryStatus.Defeated,
                "c3", CountryStatus.Ready));

        List<Diff> diffs = new DefeatedChecker().check(previous, current);
        assertThat("Unexpected diffs", diffs, hasSize(2));
        assertThat(
                "Unexpected message",
                diffs.stream().map(Diff::getMessage).collect(toList()),
                containsInAnyOrder("c1 has been defeated", "c2 has been defeated"));

    }

    private Snapshot makeSnapshotWithCountryStatus(Map<String, CountryStatus> countries) {
        return Snapshot.create(ZonedDateTime.now(), GameState.builder()
                .name("test")
                .id(1)
                .date(GameDate.create(Season.Spring, 1901))
                .phase(GamePhase.Diplomacy)
                .countries(EntryStream.of(countries)
                        .mapKeyValue((c, s) -> CountryState.builder()
                                .countryName(c)
                                .status(s)
                                .user(UserInfo.create("test", 1))
                                .supplyCenterCount(0)
                                .unitCount(0)
                                .build())
                        .collect(toList()))
                .build());
    }
}
