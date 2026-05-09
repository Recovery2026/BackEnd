package com.example.recovery.response;

import com.example.recovery.dto.MemoirSimple;

import java.util.List;

public class MemoirSimpleResponse extends ListTotalResponse<MemoirSimple> {
    public MemoirSimpleResponse(List<MemoirSimple> list, long total) {
        super(list, total);
    }
}
