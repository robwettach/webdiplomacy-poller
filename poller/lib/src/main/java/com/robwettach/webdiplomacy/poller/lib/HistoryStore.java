package com.robwettach.webdiplomacy.poller.lib;

import java.util.List;
import java.util.Optional;

public interface HistoryStore {
    List<Snapshot> getSnapshotsForGame(int gameId);

    Optional<Snapshot> getLatestSnapshotForGame(int id);

    void addSnapshot(int gameId, Snapshot snapshot);
}
