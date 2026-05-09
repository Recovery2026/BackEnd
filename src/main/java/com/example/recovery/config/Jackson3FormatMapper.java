package com.example.recovery.config;

import java.lang.reflect.Type;

import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.JavaType;
import org.hibernate.type.format.AbstractJsonFormatMapper;

import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.json.JsonMapper;

public class Jackson3FormatMapper extends AbstractJsonFormatMapper {

    private final JsonMapper jsonMapper;

    public Jackson3FormatMapper(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    @Override
    public <T> T fromString(CharSequence charSequence, Type type) {
        try {
            if (charSequence == null) {
                return null;
            }
            return jsonMapper.readValue(
                    charSequence.toString(),
                    jsonMapper.constructType(type)
            );
        } catch (Exception e) {
            throw new IllegalArgumentException("JSON 역직렬화 실패", e);
        }
    }

    @Override
    public <T> String toString(T value, Type type) {
        try {
            if (value == null) {
                return null;
            }
            return jsonMapper.writerFor(jsonMapper.constructType(type))
                    .writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalArgumentException("JSON 직렬화 실패", e);
        }
    }

    @Override
    public <T> T readFromSource(JavaType<T> javaType, Object source, WrapperOptions options) {
        try {
            return jsonMapper.readValue(
                    (JsonParser) source,
                    jsonMapper.constructType(javaType.getJavaType())
            );
        } catch (Exception e) {
            throw new IllegalArgumentException("JSON source 역직렬화 실패", e);
        }
    }

    @Override
    public <T> void writeToTarget(T value, JavaType<T> javaType, Object target, WrapperOptions options) {
        try {
            jsonMapper.writerFor(jsonMapper.constructType(javaType.getJavaType()))
                    .writeValue((JsonGenerator) target, value);
        } catch (Exception e) {
            throw new IllegalArgumentException("JSON target 직렬화 실패", e);
        }
    }

    @Override
    public boolean supportsSourceType(Class<?> sourceType) {
        return JsonParser.class.isAssignableFrom(sourceType);
    }

    @Override
    public boolean supportsTargetType(Class<?> targetType) {
        return JsonGenerator.class.isAssignableFrom(targetType);
    }
}
