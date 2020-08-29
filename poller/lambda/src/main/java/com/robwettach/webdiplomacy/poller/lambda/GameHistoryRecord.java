package com.robwettach.webdiplomacy.poller.lambda;

import com.robwettach.webdiplomacy.model.GameState;
import java.time.ZonedDateTime;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbConvertedBy;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

/**
 * DynamoDB Model object representing a single {@link GameState}.
 */
@DynamoDbBean
public class GameHistoryRecord {
    private int gameId;
    private ZonedDateTime time;
    private GameState state;

    @DynamoDbPartitionKey
    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    @DynamoDbSortKey
    public ZonedDateTime getTime() {
        return time;
    }

    public void setTime(ZonedDateTime time) {
        this.time = time;
    }

    @DynamoDbConvertedBy(GameStateJsonConverter.class)
    public GameState getState() {
        return state;
    }

    public void setState(GameState state) {
        this.state = state;
    }
}
