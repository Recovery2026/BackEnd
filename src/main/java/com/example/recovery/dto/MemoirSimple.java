package com.example.recovery.dto;

import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
public class MemoirSimple {
    long id;
    Map<String, Object> memoir;
    LocalDate date;
}
