package com.example.recovery.repository.memoirs;

import com.example.recovery.config.QuerydslConfig;
import com.example.recovery.domain.memoirs.Memoirs;
import com.example.recovery.domain.user.Users;
import com.example.recovery.maker.MemoirsMaker;
import com.example.recovery.maker.UsersMaker;
import com.example.recovery.request.SimplePageRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Import(QuerydslConfig.class)
class MemoirRepositoryImplTest {

    @Autowired
    private MemoirRepository memoirRepository;

    @Autowired
    private TestEntityManager entityManager;

    private UsersMaker usersMaker;
    private MemoirsMaker memoirsMaker;

    @BeforeEach
    void setUp() {
        usersMaker = new UsersMaker(entityManager);
        memoirsMaker = new MemoirsMaker(entityManager);
    }

    @Test
    @DisplayName("회고 목록 조회 - userId 조건에 맞는 memoir를 date desc로 조회한다")
    void getMemoirsByRequest_filtersByUserAndSortsByDateDesc() {
        // given
        Users userA = usersMaker.persist("user-a", OffsetDateTime.parse("2026-01-01T00:00:00+09:00"));
        Users userB = usersMaker.persist("user-b", OffsetDateTime.parse("2026-01-01T00:00:00+09:00"));
        long userAId = usersMaker.extractId(userA);

        memoirsMaker.persist(userA, LocalDate.parse("2026-01-03"));
        memoirsMaker.persist(userA, LocalDate.parse("2026-01-01"));
        memoirsMaker.persist(userB, LocalDate.parse("2026-01-02"));

        entityManager.flush();
        entityManager.clear();

        SimplePageRequest simplePageRequest = new SimplePageRequest();

        // when
        List<Memoirs> result = memoirRepository.getMemoirsByRequest(userAId, simplePageRequest);

        // then
        assertEquals(2, result.size());
        Memoirs firstMemoir = result.getFirst();
        assertEquals(LocalDate.parse("2026-01-03"), firstMemoir.getDate());
        assertEquals(LocalDate.parse("2026-01-01"), result.get(1).getDate());

        Map<String, Object> memoir1 = memoirsMaker.asMap(firstMemoir.getMemoir().get("1"));
        assertEquals("2026/01/01 회고 - 오늘 공부한 것", memoir1.get("title"));
        assertEquals("TSX란?", memoirsMaker.nestedValue(memoir1, "subMemoirTitles", "1", "title"));

        Map<String, Object> improvement1 = memoirsMaker.asMap(firstMemoir.getImprovement().get("1"));
        assertEquals("TSX를 공부했다.", improvement1.get("improvement"));
        assertEquals("TSX는 React 컴포넌트 정의를 위한 TypeScript기반 파일이다.", memoirsMaker.nestedValue(improvement1, "subImprovements", "1", "improvement"));

        Map<String, Object> feedback1 = memoirsMaker.asMap(firstMemoir.getFeedback().get("1"));
        assertEquals("TSX를 공부했다.", feedback1.get("feedback"));
        assertEquals("TSX는 React 컴포넌트 정의를 위한 TypeScript기반 파일이다.", memoirsMaker.nestedValue(feedback1, "subFeedback", "1", "feedback"));
    }

    @Test
    @DisplayName("조회 결과가 없으면 빈 리스트를 반환한다")
    void getMemoirsByRequest_returnsEmptyListWhenNoRows() {
        // given
        Users userA = usersMaker.persist("user-a", OffsetDateTime.parse("2026-01-01T00:00:00+09:00"));
        memoirsMaker.persist(userA, LocalDate.parse("2026-01-05"));
        entityManager.flush();
        entityManager.clear();

        SimplePageRequest simplePageRequest = new SimplePageRequest();

        // when
        List<Memoirs> result = memoirRepository.getMemoirsByRequest(999L, simplePageRequest);

        // then
        assertTrue(result.isEmpty());
    }
}
