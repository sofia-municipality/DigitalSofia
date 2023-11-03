package com.bulpros.common;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.SneakyThrows;


public class CommonUtils {
 private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @SneakyThrows
    public static ObjectMapper ObjMapper() {
        return OBJECT_MAPPER;
    }

    @SneakyThrows
    public static ObjectMapper getGeneralMapperAcceptsEmpty() {
        return OBJECT_MAPPER.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
                .setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
    }

}
