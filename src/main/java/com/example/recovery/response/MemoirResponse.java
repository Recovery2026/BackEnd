package com.example.recovery.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Builder
@Getter
@Setter
public class MemoirResponse {
    private Map<String, Object> memoir;
    private Map<String, Object> improvement;
    private Map<String, Object> feedback;
}
