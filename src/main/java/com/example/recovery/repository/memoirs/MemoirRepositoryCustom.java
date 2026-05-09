package com.example.recovery.repository.memoirs;

import com.example.recovery.domain.memoirs.Memoirs;
import com.example.recovery.request.MemoirBodyRequest;
import com.example.recovery.request.SimplePageRequest;

import java.util.List;

public interface MemoirRepositoryCustom {
    List<Memoirs> getMemoirsByRequest(MemoirBodyRequest request, SimplePageRequest simplePageRequest);
}
