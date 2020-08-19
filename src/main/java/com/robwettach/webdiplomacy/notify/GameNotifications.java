package com.robwettach.webdiplomacy.notify;

import com.google.common.collect.ImmutableList;
import com.robwettach.webdiplomacy.model.GameState;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class GameNotifications {

    private final int gameId;
    private final Notifier notifier;

    private final List<DiffChecker> checkers = ImmutableList.of(
            new PhaseChecker(),
            new HourRemainingChecker(),
            new PausedChecker(),
            new OrderChecker(),
            new DefeatedChecker(),
            new VoteChecker());

    public GameNotifications(int gameId, Notifier notifier) {
        this.gameId = gameId;
        this.notifier = notifier;
    }

    public int getGameId() {
        return gameId;
    }

    public List<Diff> updateSilently(GameState state) {
        return checkers.stream()
                .flatMap(c -> c.check(state).stream())
                .collect(toList());
    }

    public List<Diff> updateAndNotify(GameState state) {
        List<Diff> diffs = updateSilently(state);
        notifier.notify(diffs);
        return diffs;
    }
}
