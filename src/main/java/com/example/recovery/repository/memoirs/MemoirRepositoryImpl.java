package com.example.recovery.repository.memoirs;

import com.example.recovery.domain.memoirs.Memoirs;
import com.example.recovery.domain.memoirs.QMemoirs;
import com.example.recovery.request.MemoirBodyRequest;
import com.example.recovery.request.SimplePageRequest;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MemoirRepositoryImpl implements MemoirRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<Memoirs> getMemoirsByRequest(MemoirBodyRequest request, SimplePageRequest simplePageRequest) {
        QMemoirs memoirs = QMemoirs.memoirs;
        int page = simplePageRequest.getPage();
        int rowsPerPage = simplePageRequest.getRowsPerPage();
        long offset = (long) (page - 1) * rowsPerPage;

        return queryFactory.selectFrom(memoirs)
                .where(memoirs.users.id.eq(request.getUserId()))
                .orderBy(memoirs.date.desc())
                .offset(offset)
                .limit(rowsPerPage)
                .fetch();
    }
}
