package com.example.recovery.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class MemoirUpdateRequest {
    @NotNull(message = "data는 필수 항목입니다.")
    private Map<String, Object> data;
}
