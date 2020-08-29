package com.robwettach.webdiplomacy.poller.lambda;

import static java.util.stream.Collectors.toList;

import com.google.common.annotations.VisibleForTesting;
import com.robwettach.webdiplomacy.notify.Snapshot;
import com.robwettach.webdiplomacy.poller.lib.HistoryStore;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

/**
 * {@link HistoryStore} that manages history in a DynamoDB table.
 */
public class DynamoHistoryStore implements HistoryStore {
    private static final Function<GameHistoryRecord, Snapshot> RECORD_TO_SNAPSHOT =
            r -> Snapshot.create(r.getTime(), r.getState());

    private final DynamoDbTable<GameHistoryRecord> recordsTable;

    public DynamoHistoryStore(String tableName) {
        this(tableName, DynamoDbEnhancedClient.create());
    }

    @VisibleForTesting
    DynamoHistoryStore(String tableName, DynamoDbEnhancedClient dynamo) {
        this(dynamo.table(tableName, TableSchema.fromBean(GameHistoryRecord.class)));
    }

    @VisibleForTesting
    DynamoHistoryStore(DynamoDbTable<GameHistoryRecord> recordsTable) {
        this.recordsTable = recordsTable;
    }

    @Override
    public List<Snapshot> getSnapshotsForGame(int gameId) {
        PageIterable<GameHistoryRecord> records = recordsTable.query(qb ->
                qb.queryConditional(QueryConditional.keyEqualTo(kb -> kb.partitionValue(gameId))));
        return records.items()
                .stream()
                .map(RECORD_TO_SNAPSHOT)
                .collect(toList());
    }

    @Override
    public Optional<Snapshot> getLatestSnapshotForGame(int gameId) {
        PageIterable<GameHistoryRecord> latest = recordsTable.query(qb ->
                qb.queryConditional(QueryConditional.keyEqualTo(kb -> kb.partitionValue(gameId)))
                        .scanIndexForward(false)
                        .limit(1));
        return latest.items().stream().map(RECORD_TO_SNAPSHOT).findFirst();
    }

    @Override
    public void addSnapshot(int gameId, Snapshot snapshot) {
        GameHistoryRecord record = new GameHistoryRecord();
        record.setGameId(gameId);
        record.setTime(snapshot.getTime());
        record.setState(snapshot.getState());

        recordsTable.putItem(record);
    }
}
