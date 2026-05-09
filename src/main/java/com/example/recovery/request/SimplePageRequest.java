package com.example.recovery.request;

import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SimplePageRequest {
    @Positive(message = "page는 양수여야 합니다.")
    private int page = 1;

    @Positive(message = "rowsPerPage는 양수여야 합니다.")
    private int rowsPerPage = 10;
}
