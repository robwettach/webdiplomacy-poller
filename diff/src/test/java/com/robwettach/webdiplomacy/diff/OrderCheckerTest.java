package com.robwettach.webdiplomacy.diff;

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

class OrderCheckerTest {
    @Test
    void shouldNotReportNoneSubmitted() {
        Snapshot previous = makeSnapshotWithCountryStatus(Map.of(
                "c1", CountryStatus.NoOrders,
                "c2", CountryStatus.Defeated,
                "c3", CountryStatus.NotReceived,
                "c4", CountryStatus.NotReceived));
        Snapshot current = makeSnapshotWithCountryStatus(Map.of(
                "c1", CountryStatus.NoOrders,
                "c2", CountryStatus.Defeated,
                "c3", CountryStatus.NotReceived,
                "c4", CountryStatus.NotReceived));

        List<Diff> diffs = new OrderChecker().check(previous, current);
        assertThat("Unexpected diffs", diffs, is(empty()));
    }

    @Test
    void shouldNotReportNoneReady() {
        Snapshot previous = makeSnapshotWithCountryStatus(Map.of(
                "c1", CountryStatus.NoOrders,
                "c2", CountryStatus.NotReceived,
                "c3", CountryStatus.NotReceived,
                "c4", CountryStatus.NotReceived));
        Snapshot current = makeSnapshotWithCountryStatus(Map.of(
                "c1", CountryStatus.NoOrders,
                "c2", CountryStatus.Completed,
                "c3", CountryStatus.Completed,
                "c4", CountryStatus.Completed));

        List<Diff> diffs = new OrderChecker().check(previous, current);
        assertThat("Unexpected diffs", diffs, is(empty()));
    }

    @Test
    void shouldNotReportMultipleSubmitted() {
        Snapshot previous = makeSnapshotWithCountryStatus(Map.of(
                "c1", CountryStatus.NotReceived,
                "c2", CountryStatus.NotReceived,
                "c3", CountryStatus.NotReceived,
                "c4", CountryStatus.NotReceived));
        Snapshot current = makeSnapshotWithCountryStatus(Map.of(
                "c1", CountryStatus.NotReceived,
                "c2", CountryStatus.NotReceived,
                "c3", CountryStatus.Completed,
                "c4", CountryStatus.Completed));

        List<Diff> diffs = new OrderChecker().check(previous, current);
        assertThat("Unexpected diffs", diffs, is(empty()));
    }

    @Test
    void shouldNotReportMultipleReady() {
        Snapshot previous = makeSnapshotWithCountryStatus(Map.of(
                "c1", CountryStatus.Completed,
                "c2", CountryStatus.Completed,
                "c3", CountryStatus.Completed,
                "c4", CountryStatus.Completed));
        Snapshot current = makeSnapshotWithCountryStatus(Map.of(
                "c1", CountryStatus.Completed,
                "c2", CountryStatus.Completed,
                "c3", CountryStatus.Ready,
                "c4", CountryStatus.Ready));

        List<Diff> diffs = new OrderChecker().check(previous, current);
        assertThat("Unexpected diffs", diffs, is(empty()));
    }

    @Test
    void shouldNotReportAllSubmitted() {
        Snapshot previous = makeSnapshotWithCountryStatus(Map.of(
                "c1", CountryStatus.NotReceived,
                "c2", CountryStatus.NotReceived,
                "c3", CountryStatus.Completed,
                "c4", CountryStatus.Completed));
        Snapshot current = makeSnapshotWithCountryStatus(Map.of(
                "c1", CountryStatus.Completed,
                "c2", CountryStatus.Completed,
                "c3", CountryStatus.Completed,
                "c4", CountryStatus.Completed));

        List<Diff> diffs = new OrderChecker().check(previous, current);
        assertThat("Unexpected diffs", diffs, is(empty()));
    }

    @Test
    void shouldReportOnlyOneNotSubmitted() {
        Snapshot previous = makeSnapshotWithCountryStatus(Map.of(
                "c1", CountryStatus.NotReceived,
                "c2", CountryStatus.NotReceived,
                "c3", CountryStatus.Completed,
                "c4", CountryStatus.Completed));
        Snapshot current = makeSnapshotWithCountryStatus(Map.of(
                "c1", CountryStatus.NotReceived,
                "c2", CountryStatus.Completed,
                "c3", CountryStatus.Completed,
                "c4", CountryStatus.Completed));

        List<Diff> diffs = new OrderChecker().check(previous, current);
        assertThat("Unexpected diffs", diffs, hasSize(1));
        assertThat("Unexpected message", diffs.get(0).getMessage(), is("Only c1 has not yet submitted orders"));
    }

    @Test
    void shouldReportOnlyOneNotReady() {
        Snapshot previous = makeSnapshotWithCountryStatus(Map.of(
                "c1", CountryStatus.Completed,
                "c2", CountryStatus.Completed,
                "c3", CountryStatus.Ready,
                "c4", CountryStatus.Ready));
        Snapshot current = makeSnapshotWithCountryStatus(Map.of(
                "c1", CountryStatus.Completed,
                "c2", CountryStatus.Ready,
                "c3", CountryStatus.Ready,
                "c4", CountryStatus.Ready));

        List<Diff> diffs = new OrderChecker().check(previous, current);
        assertThat("Unexpected diffs", diffs, hasSize(1));
        assertThat("Unexpected message", diffs.get(0).getMessage(), is("Only c1 is not yet ready"));
    }

    @Test
    void shouldNotReportSameCountryNotSubmittedAndNotReady() {
        Snapshot previous = makeSnapshotWithCountryStatus(Map.of(
                "c1", CountryStatus.NotReceived,
                "c2", CountryStatus.Completed,
                "c3", CountryStatus.Ready,
                "c4", CountryStatus.Ready));
        Snapshot current = makeSnapshotWithCountryStatus(Map.of(
                "c1", CountryStatus.NotReceived,
                "c2", CountryStatus.Ready,
                "c3", CountryStatus.Ready,
                "c4", CountryStatus.Ready));

        List<Diff> diffs = new OrderChecker().check(previous, current);
        assertThat("Unexpected message", diffs.get(0).getMessage(), is("Only c1 has not yet submitted orders"));
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
