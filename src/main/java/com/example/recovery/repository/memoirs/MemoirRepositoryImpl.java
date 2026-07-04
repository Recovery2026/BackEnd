package com.example.recovery.repository.memoirs;

import com.example.recovery.domain.memoirs.Memoirs;
import com.example.recovery.domain.memoirs.QMemoirs;
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
    public List<Memoirs> getMemoirsByRequest(Long userId, SimplePageRequest simplePageRequest) {
        QMemoirs memoirs = QMemoirs.memoirs;
        int page = simplePageRequest.getPage();
        int rowsPerPage = simplePageRequest.getRowsPerPage();
        long offset = (long) (page - 1) * rowsPerPage;

        return queryFactory.selectFrom(memoirs)
                .where(memoirs.users.id.eq(userId))
                .orderBy(memoirs.date.desc())
                .offset(offset)
                .limit(rowsPerPage)
                .fetch();
    }

    @Override
    public long countMemoirsByRequest(Long userId, SimplePageRequest simplePageRequest) {
        QMemoirs memoirs = QMemoirs.memoirs;

        Long count = queryFactory.select(memoirs.count())
                .from(memoirs)
                .where(memoirs.users.id.eq(userId))
                .fetchOne();

        return count == null ? 0L : count;
    }
}
