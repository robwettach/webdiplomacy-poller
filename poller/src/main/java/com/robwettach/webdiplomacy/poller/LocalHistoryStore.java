package com.robwettach.webdiplomacy.poller;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Verify.verify;
import static com.robwettach.webdiplomacy.json.Json.OBJECT_MAPPER;
import static java.lang.String.format;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.robwettach.webdiplomacy.poller.lib.HistoryStore;
import com.robwettach.webdiplomacy.poller.lib.Snapshot;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LocalHistoryStore implements HistoryStore {
    private static final String GAME_SNAPSHOTS_FORMAT = "%d-snapshots.json";
    private static final String GAME_ID_KEY = "gameId";
    private static final Pattern GAME_SNAPSHOTS_PATTERN = Pattern.compile(format(
            "(?<%s>\\d+)-snapshots.json",
            GAME_ID_KEY));
    private static final String LEGACY_SNAPSHOTS_FILE_NAME = "snapshots.json";

    private final Path configDirPath;
    private final Path snapshotsPath;

    private Map<Integer, List<Snapshot>> snapshots = new HashMap<>();

    public LocalHistoryStore(Path configDirPath) {
        checkNotNull(configDirPath, "configDirPath must not be null");
        this.configDirPath = configDirPath;
        this.snapshotsPath = configDirPath.resolve(LEGACY_SNAPSHOTS_FILE_NAME);
        load();
    }

    private void load() {
        loadLegacySnapshots();
        try {
            Files.list(configDirPath)
                    .filter(p -> GAME_SNAPSHOTS_PATTERN.matcher(p.getFileName().toString()).find())
                    .forEach((path) -> {
                        Matcher gameIdMatcher = GAME_SNAPSHOTS_PATTERN.matcher(path.getFileName().toString());
                        verify(
                                gameIdMatcher.find(),
                                "Failed to parse filename that was already filtered to match! %s",
                                path.getFileName());
                        int gameId = Integer.parseInt(gameIdMatcher.group(GAME_ID_KEY));
                        List<Snapshot> gameSnapshots = new ArrayList<>();
                        try {
                            gameSnapshots = OBJECT_MAPPER.readValue(
                                    path.toFile(),
                                    new TypeReference<>() {});
                        } catch (IOException e) {
                            System.err.println("Failed to read JSON from: " + path);
                            e.printStackTrace();
                        }
                        snapshots.put(gameId, gameSnapshots);
                        System.out.printf(
                                "Loaded %d snapshots from game %d from: %s%n",
                                gameSnapshots.size(),
                                gameId,
                                path);
                    });
        } catch (IOException e) {
            System.err.println("Failed to list files in: " + configDirPath);
            e.printStackTrace();
        }

    }

    private void loadLegacySnapshots() {
        verify(snapshots.isEmpty(), "Cannot load legacy snapshots after initializing");
        if (Files.exists(snapshotsPath)) {
            System.out.println("Found legacy snapshots file: " + snapshotsPath);
            try {
                // Make a known-mutable copy
                snapshots = new HashMap<>(OBJECT_MAPPER.readValue(
                        snapshotsPath.toFile(),
                        new TypeReference<>() {}));
            } catch (IOException e) {
                System.err.println("Failed to read JSON from: " + snapshotsPath);
                e.printStackTrace();
            }
            int snapshotCount = snapshots.values().stream().mapToInt(List::size).sum();
            System.out.printf(
                    "Loaded %d snapshots from %d games from: %s%n",
                    snapshotCount,
                    snapshots.size(),
                    snapshotsPath);

            // Save the loaded snapshots out to the new format
            save();
            // Delete the legacy file
            try {
                Files.deleteIfExists(snapshotsPath);
                System.out.printf("Deleted legacy snapshots file: %s%n", snapshotsPath);
            } catch (IOException e) {
                System.err.println("Failed to delete legacy snapshots file: " + snapshotsPath);
                e.printStackTrace();
            }
        }
    }

    @Override
    public ImmutableList<Snapshot> getSnapshotsForGame(int gameId) {
        return ImmutableList.copyOf(snapshots.getOrDefault(gameId, new ArrayList<>()));
    }

    @Override
    public Optional<Snapshot> getLatestSnapshotForGame(int id) {
        List<Snapshot> gameSnapshots = getSnapshotsForGame(id);
        return Optional.ofNullable(Iterables.getLast(gameSnapshots, null));
    }

    @Override
    public void addSnapshot(int gameId, Snapshot snapshot) {
        List<Snapshot> gameSnapshots = snapshots.computeIfAbsent(gameId, (x) -> new ArrayList<>());
        gameSnapshots.add(snapshot);
        save();
    }

    private void save() {
        snapshots.forEach((gameId, gameSnapshots) -> {
            Path path = configDirPath.resolve(format(GAME_SNAPSHOTS_FORMAT, gameId));
            try {
                OBJECT_MAPPER.writeValue(path.toFile(), gameSnapshots);
                System.out.printf(
                        "Saved %d snapshots from game %d to: %s%n",
                        gameSnapshots.size(),
                        gameId,
                        path);
            } catch (IOException e) {
                System.err.println("Failed to write snapshots to: " + path);
                e.printStackTrace();
            }
        });
    }
}
