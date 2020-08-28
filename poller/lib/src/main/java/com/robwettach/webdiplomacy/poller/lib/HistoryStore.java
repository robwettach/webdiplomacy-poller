package com.robwettach.webdiplomacy.poller.lib;

import com.robwettach.webdiplomacy.notify.Snapshot;
import java.util.List;
import java.util.Optional;

/**
 * Interface defining interactions with a history store tracking {@link Snapshot}s of a <em>webDiplomacy</em> game.
 */
public interface HistoryStore {
    /**
     * Get all snapshots for a given game.
     *
     * @param gameId The ID of the game for which to retrieve snapshots
     * @return The {@link List} of all snapshots for the given {@code gameId}.  Not-{@code null}.
     *         If no {@link Snapshot}s are found, an empty {@link List} is returned.
     */
    List<Snapshot> getSnapshotsForGame(int gameId);

    /**
     * Get the most recent snapshot for a given game.
     *
     * @param gameId the ID of the game for which to retrieve a snapshot
     * @return An {@link Optional} containing the most recent {@link Snapshot}, if present.
     *         Else, {@link Optional#empty()}.
     */
    Optional<Snapshot> getLatestSnapshotForGame(int gameId);

    /**
     * Add a {@link Snapshot} for a given game.
     *
     * @param gameId The Id of the game for which to add a snapshot
     * @param snapshot The {@link Snapshot} to add
     */
    void addSnapshot(int gameId, Snapshot snapshot);
}
