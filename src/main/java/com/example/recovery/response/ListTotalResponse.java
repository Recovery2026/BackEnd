package com.example.recovery.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class ListTotalResponse<T> {
    private final List<T> list;
    private final long total;
}
