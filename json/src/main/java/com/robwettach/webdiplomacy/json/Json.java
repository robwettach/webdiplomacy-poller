package com.robwettach.webdiplomacy.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

/**
 * Wrapper class to hold the common {@link ObjectMapper} instance for <em>webDiplomacy</em>.
 */
public class Json {
    /**
     * Common {@link ObjectMapper} instance to be used to serialized/deserialize JSON within <em>webDiplomacy</em>.
     */
    public static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder()
            .addModule(new GuavaModule())
            .addModule(new JavaTimeModule())
            .addModule(new Jdk8Module())
            .addModule(new ParameterNamesModule())
            .serializationInclusion(JsonInclude.Include.NON_ABSENT)
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false)
            .build();
}
