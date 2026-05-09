package com.example.recovery.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.Map;

@Getter
@Setter
public class MemoirWriteRequest {
    @NotNull(message = "userId는 필수 항목입니다.")
    @Positive(message = "userId는 양수여야 합니다.")
    private Long userId;

    @NotNull(message = "data는 필수 항목입니다.")
    private Map<String, Object> data;

    @NotNull(message = "date는 필수 항목입니다.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE, pattern = "yyyy-MM-dd")
    private LocalDate date;
}
