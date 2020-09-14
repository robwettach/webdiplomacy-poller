package com.robwettach.webdiplomacy.poller.lib;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import com.robwettach.webdiplomacy.diff.Diff;
import com.robwettach.webdiplomacy.diff.DiffCheckers;
import com.robwettach.webdiplomacy.diff.Snapshot;
import com.robwettach.webdiplomacy.model.CountryState;
import com.robwettach.webdiplomacy.model.CountryStatus;
import com.robwettach.webdiplomacy.model.GameDate;
import com.robwettach.webdiplomacy.model.GamePhase;
import com.robwettach.webdiplomacy.model.GameState;
import com.robwettach.webdiplomacy.model.UserInfo;
import com.robwettach.webdiplomacy.model.Vote;
import com.robwettach.webdiplomacy.notify.Notifier;
import com.robwettach.webdiplomacy.page.CountryUserLink;
import com.robwettach.webdiplomacy.page.GameBoardPage;
import com.robwettach.webdiplomacy.page.GameTitleBar;
import com.robwettach.webdiplomacy.page.MemberRow;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Main entrypoint for webDiplomacy Poller applications.
 */
public class Poller {
    private static final Logger LOG = LogManager.getLogger(Poller.class);

    private final int gameId;
    private final HistoryStore history;
    private final Notifier notifier;

    /**
     * Create a {@link Poller} for a given game.
     *
     * @param gameId The ID of the game to poll
     * @param history The {@link HistoryStore} to store and retrieve history to/from
     * @param notifier The {@link Notifier} to send notifications to
     */
    public Poller(int gameId, HistoryStore history, Notifier notifier) {
        this.gameId = gameId;
        this.history = history;
        this.notifier = notifier;
    }

    /**
     * Poll the current status of a <em>webDiplomacy</em> game, send notifications, and update the history.
     */
    public void poll() {
        LOG.debug("Polling for changes to game {}", gameId);
        GameBoardPage page;
        try {
            page = GameBoardPage.loadGame(gameId);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load webDiplomacy game board page for game: " + gameId, e);
        }

        GameState state = stateFromPage(gameId, page);

        ZonedDateTime snapshotDate = ZonedDateTime.now(ZoneOffset.UTC);
        Snapshot current = Snapshot.create(snapshotDate, state);

        Optional<Snapshot> previous = history.getLatestSnapshotForGame(gameId);
        List<Diff> diffs = previous.map(p -> DiffCheckers.check(p, current)).orElse(Collections.emptyList());
        LOG.info("Found {} diffs at {} for game {}", diffs.size(), snapshotDate, gameId);
        notifier.notify(diffs);

        // Diffs imply a change, and a change implies diffs, but it's not necessarily 1-to-1
        // We track more pieces of state than we notify about (SC/unit count, etc), and we want to notify
        // even if there's no state change, specifically for the "one hour remaining" case.
        if (previous.isEmpty() || !previous.get().getState().equals(current.getState())) {
            history.addSnapshot(gameId, current);
        }
    }

    private static GameState stateFromPage(int gameId, GameBoardPage page) {
        GameState.Builder builder = GameState.builder()
                .id(gameId);

        GameTitleBar titleBar = page.getTitleBar();
        builder.name(titleBar.getName())
                .date(GameDate.parse(titleBar.getDate()))
                .phase(GamePhase.fromString(titleBar.getPhase()))
                .paused(titleBar.isPaused())
                .finished(titleBar.isFinished())
                .nextTurnAt(titleBar.getNextTurnAt().orElse(null));

        builder.countries(page.getMembersTable()
                .stream()
                .flatMap(t -> t.getRows().stream())
                .map(Poller::countryFromRow)
                .collect(toList()));

        return builder.build();
    }

    private static CountryState countryFromRow(MemberRow memberRow) {
        return CountryState.builder()
                .countryName(memberRow.getCountryName())
                .user(userInfoFromLink(memberRow.getUser()))
                .status(CountryStatus.fromString(memberRow.getStatus()))
                .supplyCenterCount(memberRow.getSupplyCenterCount())
                .unitCount(memberRow.getUnitCount())
                .votes(memberRow.getVotes().stream().map(Vote::valueOf).collect(toSet()))
                .build();
    }

    private static UserInfo userInfoFromLink(CountryUserLink link) {
        return UserInfo.create(link.getName(), link.getId());
    }
}
