package com.robwettach.webdiplomacy.poller.lambda;

import com.robwettach.webdiplomacy.model.GameState;

/**
 * {@link software.amazon.awssdk.enhanced.dynamodb.AttributeConverter AttributeConverter} that translates between
 * {@link GameState} instances and their specific JSON representation.
 */
public class GameStateJsonConverter extends JsonAttributeConverter<GameState> {
    public GameStateJsonConverter() {
        super(GameState.class);
    }
}
