package com.robwettach.webdiplomacy.diff;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.robwettach.webdiplomacy.model.CountryState;
import com.robwettach.webdiplomacy.model.CountryStatus;
import com.robwettach.webdiplomacy.model.GameDate;
import com.robwettach.webdiplomacy.model.GamePhase;
import com.robwettach.webdiplomacy.model.GameState;
import com.robwettach.webdiplomacy.model.Season;
import com.robwettach.webdiplomacy.model.UserInfo;
import com.robwettach.webdiplomacy.model.Vote;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import one.util.streamex.EntryStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class VoteCheckerTest {
    @ParameterizedTest
    @EnumSource
    void shouldNotifyUponStartingVote(Vote vote) {
        Snapshot previous = makeSnapshot(Map.of("Test", Set.of()));
        Snapshot current = makeSnapshot(Map.of("Test", Set.of(vote)));

        List<Diff> diffs = new VoteChecker().check(previous, current);
        assertThat("Unexpected number of diffs", diffs, hasSize(1));
        Diff diff = diffs.get(0);
        assertThat("Unexpected message", diff.getMessage(), is(String.format("Test is starting a \"%s\" vote", vote)));
    }

    @ParameterizedTest
    @EnumSource
    void shouldNotifyForLastCountryToVote(Vote vote) {
        Snapshot previous = makeSnapshot(Map.of(
                "Test1", Set.of(),
                "Test2", Set.of(),
                "Test3", Set.of(vote)));
        Snapshot current = makeSnapshot(Map.of(
                "Test1", Set.of(),
                "Test2", Set.of(vote),
                "Test3", Set.of(vote)));

        List<Diff> diffs = new VoteChecker().check(previous, current);
        assertThat("Unexpected number of diffs", diffs, hasSize(1));
        Diff diff = diffs.get(0);
        assertThat(
                "Unexpected message",
                diff.getMessage(),
                is(String.format("Only Test1 has not voted \"%s\" yet", vote)));

    }

    @Test
    void shouldNotifyForEachVoteStartedByOneCountry() {
        Snapshot previous = makeSnapshot(Map.of("Test", Set.of()));
        Set<Vote> votes = Set.of(Vote.Draw, Vote.Pause, Vote.Unpause);
        Snapshot current = makeSnapshot(Map.of("Test", votes));

        List<Diff> diffs = new VoteChecker().check(previous, current);
        assertThat("Unexpected number of diffs", diffs, hasSize(votes.size()));
        String[] expectedMessages = votes.stream()
                .map(vote -> String.format("Test is starting a \"%s\" vote", vote))
                .toArray(String[]::new);
        assertThat(
                "Unexpected diffs",
                diffs.stream().map(Diff::getMessage).collect(toList()),
                containsInAnyOrder(expectedMessages));
    }

    @Test
    void shouldNotifyForEachLastVoteByOneCountry() {
        Set<Vote> votes = Set.of(Vote.Draw, Vote.Pause, Vote.Unpause);
        Snapshot previous = makeSnapshot(Map.of(
                "Test1", Set.of(),
                "Test2", Set.of(),
                "Test3", votes));
        Snapshot current = makeSnapshot(Map.of(
                "Test1", Set.of(),
                "Test2", votes,
                "Test3", votes));

        List<Diff> diffs = new VoteChecker().check(previous, current);
        assertThat("Unexpected number of diffs", diffs, hasSize(votes.size()));
        String[] expectedMessages = votes.stream()
                .map(vote -> String.format("Only Test1 has not voted \"%s\" yet", vote))
                .toArray(String[]::new);
        assertThat(
                "Unexpected diffs",
                diffs.stream().map(Diff::getMessage).collect(toList()),
                containsInAnyOrder(expectedMessages));
    }

    @Test
    void shouldNotifyForEachVoteStartedByVariousCountries() {
        Snapshot previous = makeSnapshot(Map.of(
                "Test1", Set.of(),
                "Test2", Set.of(),
                "Test3", Set.of()));
        Map<String, Set<Vote>> countryVotes = Map.of(
                "Test1", Set.of(Vote.Draw),
                "Test2", Set.of(Vote.Pause),
                "Test3", Set.of(Vote.Unpause));
        Snapshot current = makeSnapshot(countryVotes);

        List<Diff> diffs = new VoteChecker().check(previous, current);
        assertThat("Unexpected number of diffs", diffs, hasSize(countryVotes.size()));
        String[] expectedMessages = EntryStream.of(countryVotes)
                .mapKeyValue((c, v) -> String.format("%s is starting a \"%s\" vote", c, Iterables.getOnlyElement(v)))
                .toArray(String[]::new);
        assertThat(
                "Unexpected diffs",
                diffs.stream().map(Diff::getMessage).collect(toList()),
                containsInAnyOrder(expectedMessages));
    }

    @Test
    void shouldNotifyForEachLastVoteForVariousCountries() {
        Snapshot previous = makeSnapshot(Map.of(
                "Test1", Set.of(Vote.Draw),
                "Test2", Set.of(Vote.Pause),
                "Test3", Set.of(Vote.Unpause)));
        Map<String, Set<Vote>> countryVotes = Map.of(
                "Test1", Set.of(Vote.Draw, Vote.Pause),
                // Yeah, I know that's impossible...
                "Test2", Set.of(Vote.Pause, Vote.Unpause),
                "Test3", Set.of(Vote.Unpause, Vote.Draw));
        Snapshot current = makeSnapshot(countryVotes);

        List<Diff> diffs = new VoteChecker().check(previous, current);
        assertThat("Unexpected number of diffs", diffs, hasSize(countryVotes.size()));
        Set<Vote> allVotes = Set.of(Vote.Draw, Vote.Pause, Vote.Unpause);
        String[] expectedMessages = EntryStream.of(countryVotes)
                .mapKeyValue((c, v) -> String.format(
                        "Only %s has not voted \"%s\" yet",
                        c,
                        Iterables.getOnlyElement(Sets.difference(allVotes, v))))
                .toArray(String[]::new);
        assertThat(
                "Unexpected diffs",
                diffs.stream().map(Diff::getMessage).collect(toList()),
                containsInAnyOrder(expectedMessages));
    }

    @Test
    void shouldNotNotifyForTheLastCountryToCancelAVote() {
        Snapshot previous = makeSnapshot(Map.of(
                "Test1", Set.of(Vote.Draw),
                "Test2", Set.of(Vote.Draw),
                "Test3", Set.of(Vote.Draw)));
        Snapshot current = makeSnapshot(Map.of(
                "Test1", Set.of(Vote.Draw)));

        List<Diff> diffs = new VoteChecker().check(previous, current);
        assertThat("Unexpected diff", diffs, is(empty()));
    }

    private Snapshot makeSnapshot(Map<String, Set<Vote>> countryVotes) {
        return Snapshot.create(ZonedDateTime.now(), GameState.builder()
                .name("test")
                .id(1)
                .date(GameDate.create(Season.Spring, 1901))
                .phase(GamePhase.Diplomacy)
                .countries(EntryStream.of(countryVotes)
                        .mapKeyValue((c, v) -> CountryState.builder()
                                .countryName(c)
                                .user(UserInfo.create("test", 1))
                                .status(CountryStatus.NoOrders)
                                .supplyCenterCount(0)
                                .unitCount(0)
                                .votes(v)
                                .build())
                        .collect(toList()))
                .build());
    }
}
