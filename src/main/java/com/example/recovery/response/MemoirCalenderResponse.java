package com.example.recovery.response;

import com.example.recovery.dto.MemoirSimple;

import java.time.LocalDate;
import java.util.Map;

public class MemoirCalenderResponse extends MemoirSimple {

    public MemoirCalenderResponse(long id, Map<String, Object> memoir, LocalDate date) {
        super(id, memoir, date);
    }
}
