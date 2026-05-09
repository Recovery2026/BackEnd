package com.example.recovery.maker;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class JsonMaker {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public Map<String, Object> parseJsonToMap(String json) {
        try {
            return OBJECT_MAPPER.readValue(json, new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new IllegalStateException("테스트 JSON 파싱에 실패했습니다.", e);
        }
    }
}
