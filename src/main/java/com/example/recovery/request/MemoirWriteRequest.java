package com.example.recovery.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.Map;

@Getter
@Setter
public class MemoirWriteRequest {
    @NotNull(message = "data는 필수 항목입니다.")
    private Map<String, Object> data;

    @NotNull(message = "date는 필수 항목입니다.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE, pattern = "yyyy-MM-dd")
    private LocalDate date;
}
