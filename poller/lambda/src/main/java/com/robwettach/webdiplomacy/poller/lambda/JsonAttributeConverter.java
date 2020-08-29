package com.robwettach.webdiplomacy.poller.lambda;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.robwettach.webdiplomacy.json.Json;
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

/**
 * {@link AttributeConverter} that translates between instances of a given
 * {@link #type} and their specific JSON representation.
 *
 * @param <T> The type of object to convert
 */
public class JsonAttributeConverter<T> implements AttributeConverter<T> {
    private final Class<T> type;

    JsonAttributeConverter(Class<T> type) {
        this.type = type;
    }

    @Override
    public AttributeValue transformFrom(T input) {
        try {
            return AttributeValue.builder()
                    .s(Json.OBJECT_MAPPER.writeValueAsString(input))
                    .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public T transformTo(AttributeValue input) {
        try {
            return Json.OBJECT_MAPPER.readValue(input.s(), type);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public EnhancedType<T> type() {
        return EnhancedType.of(type);
    }

    @Override
    public AttributeValueType attributeValueType() {
        return AttributeValueType.S;
    }
}
