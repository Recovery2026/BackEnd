package com.example.recovery.service.memoirs;

import com.example.recovery.common.exception.MemoirAlreadyExistException;
import com.example.recovery.common.exception.MemoirNotFoundException;
import com.example.recovery.common.exception.UsersNotFoundException;
import com.example.recovery.domain.memoirs.Memoirs;
import com.example.recovery.domain.user.Users;
import com.example.recovery.dto.MemoirSimple;
import com.example.recovery.repository.memoirs.MemoirRepository;
import com.example.recovery.repository.users.UsersRepository;
import com.example.recovery.request.MemoirCalenderRequest;
import com.example.recovery.request.MemoirUpdateRequest;
import com.example.recovery.request.MemoirWriteRequest;
import com.example.recovery.request.SimplePageRequest;
import com.example.recovery.response.MemoirCalenderResponse;
import com.example.recovery.response.MemoirResponse;
import com.example.recovery.response.MemoirSimpleResponse;
import com.example.recovery.service.auth.AuthTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MemoirServiceTest {

    private static final Long CURRENT_USER_ID = 1L;

    @Mock
    private MemoirRepository memoirRepository;

    @Mock
    private UsersRepository usersRepository;

    @Mock
    private AuthTokenService authTokenService;

    @InjectMocks
    private MemoirService memoirService;

    @BeforeEach
    void setUp() {
        given(authTokenService.getCurrentUserId()).willReturn(CURRENT_USER_ID);
    }

    @Test
    @DisplayName("회고 목록 조회 서비스 - 페이징 처리")
    void getMemoirList_returnsPagedMemoirs() {
        // given
        Memoirs memoir1 = new Memoirs();
        memoir1.setId(1L);
        memoir1.setMemoir(Map.of("title", "기록1", "content", "내용1"));
        memoir1.setDate(LocalDate.parse("2026-01-01"));

        Memoirs memoir2 = new Memoirs();
        memoir2.setId(1L);
        memoir2.setMemoir(Map.of("title", "기록2", "content", "내용2"));
        memoir2.setDate(LocalDate.parse("2026-01-02"));

        Long userId = CURRENT_USER_ID;
        SimplePageRequest simplePageRequest = new SimplePageRequest();

        given(memoirRepository.getMemoirsByRequest(userId, simplePageRequest)).willReturn(List.of(memoir1, memoir2));
        given(memoirRepository.countMemoirsByRequest(userId, simplePageRequest)).willReturn(2L);

        // when
        MemoirSimpleResponse response = memoirService.getMemoirList(simplePageRequest);

        // then
        assertEquals(2, response.getTotal());
        assertEquals(2, response.getList().size());

        MemoirSimple result = response.getList().getFirst();
        assertEquals(1L, result.getId());
        assertEquals("기록1", result.getMemoir().get("title"));
        assertEquals(LocalDate.parse("2026-01-01"), result.getDate());

        MemoirSimple result2 = response.getList().get(1);
        assertEquals(1L, result2.getId());
        assertEquals("기록2", result2.getMemoir().get("title"));
        assertEquals(LocalDate.parse("2026-01-02"), result2.getDate());
    }

    @Test
    @DisplayName("회고 캘린더 조회 서비스 - 정상 케이스")
    void getMemoirCalender_returnsMemoir() {
        // given
        Memoirs memoir = new Memoirs();
        memoir.setId(1L);
        memoir.setMemoir(Map.of("title", "캘린더 회고", "content", "내용"));
        memoir.setDate(LocalDate.parse("2026-01-01"));

        MemoirCalenderRequest calenderRequest = new MemoirCalenderRequest();
        calenderRequest.setDate(LocalDate.parse("2026-05-02"));

        given(memoirRepository.findByUsersIdAndDate(CURRENT_USER_ID, calenderRequest.getDate())).willReturn(Optional.of(memoir));

        // when
        MemoirCalenderResponse response = memoirService.getMemoirCalender(calenderRequest);

        // then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("캘린더 회고", response.getMemoir().get("title"));
        assertEquals(LocalDate.parse("2026-01-01"), response.getDate());
    }

    @Test
    @DisplayName("회고 캘린더 조회 서비스 - 해당 날짜 회고 없음 예외")
    void getMemoirCalender_throwsWhenNotFound() {
        // given
        MemoirCalenderRequest calenderRequest = new MemoirCalenderRequest();
        calenderRequest.setDate(LocalDate.parse("2026-05-03"));

        given(memoirRepository.findByUsersIdAndDate(CURRENT_USER_ID, calenderRequest.getDate())).willReturn(Optional.empty());

        // when / then
        assertThrows(MemoirNotFoundException.class, () -> memoirService.getMemoirCalender(calenderRequest));
    }

    @Test
    @DisplayName("회고 단건 조회 서비스 - 정상 케이스")
    void getMemoir_returnsMemoirResponse() {
        // given
        Memoirs memoir = new Memoirs();
        memoir.setId(1L);
        memoir.setMemoir(Map.of("title", "회고", "content", "내용"));
        memoir.setImprovement(Map.of("title", "개선", "content", "개선 내용"));
        memoir.setFeedback(Map.of("title", "피드백", "content", "피드백 내용"));

        given(memoirRepository.findByIdAndUsersId(1L, CURRENT_USER_ID)).willReturn(Optional.of(memoir));

        // when
        MemoirResponse response = memoirService.getMemoir(1L);

        // then
        assertNotNull(response);
        assertEquals("회고", response.getMemoir().get("title"));
        assertEquals("개선", response.getImprovement().get("title"));
        assertEquals("피드백", response.getFeedback().get("title"));
    }

    @Test
    @DisplayName("회고 단건 조회 서비스 - 회고 없음 예외")
    void getMemoir_throwsWhenNotFound() {
        // given
        given(memoirRepository.findByIdAndUsersId(1L, CURRENT_USER_ID)).willReturn(Optional.empty());

        // when / then
        assertThrows(MemoirNotFoundException.class, () -> memoirService.getMemoir(1L));
    }

    @Test
    @DisplayName("회고 수정 서비스 - 정상 케이스")
    void updateMemoir_updatesMemoirData() {
        // given
        Memoirs memoir = new Memoirs();
        memoir.setId(1L);
        memoir.setMemoir(Map.of("title", "기존 회고", "content", "기존 내용"));

        MemoirUpdateRequest request = new MemoirUpdateRequest();
        request.setData(Map.of("title", "수정된 회고", "content", "수정된 내용"));

        given(memoirRepository.findByIdAndUsersId(1L, CURRENT_USER_ID)).willReturn(Optional.of(memoir));

        // when
        memoirService.updateMemoir(request, 1L);

        // then
        assertEquals("수정된 회고", memoir.getMemoir().get("title"));
        assertEquals("수정된 내용", memoir.getMemoir().get("content"));
        verify(memoirRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("회고 수정 서비스 - 회고 없음 예외")
    void updateMemoir_throwsWhenNotFound() {
        // given
        MemoirUpdateRequest request = new MemoirUpdateRequest();
        request.setData(Map.of("title", "수정된 회고", "content", "수정된 내용"));

        given(memoirRepository.findByIdAndUsersId(1L, CURRENT_USER_ID)).willReturn(Optional.empty());

        // when / then
        assertThrows(MemoirNotFoundException.class, () -> memoirService.updateMemoir(request, 1L));
    }

    @Test
    @DisplayName("회고 작성 서비스 - 정상 케이스")
    void writeMemoir_savesMemoir() {
        // given
        Users users = new Users();
        ReflectionTestUtils.setField(users, "id", 1L);

        MemoirWriteRequest request = new MemoirWriteRequest();
        request.setData(Map.of("title", "새 회고", "content", "새 내용"));
        request.setDate(LocalDate.now().minusDays(1));

        given(usersRepository.findById(CURRENT_USER_ID)).willReturn(Optional.of(users));
        given(memoirRepository.findByUsersIdAndDate(CURRENT_USER_ID, request.getDate())).willReturn(Optional.empty());

        // when
        memoirService.writeMemoir(request);

        // then
        verify(memoirRepository).save(any(Memoirs.class));
    }

    @Test
    @DisplayName("회고 작성 서비스 - 사용자 없음 예외")
    void writeMemoir_throwsWhenUserNotFound() {
        // given
        MemoirWriteRequest request = new MemoirWriteRequest();
        request.setData(Map.of("title", "새 회고", "content", "새 내용"));
        request.setDate(LocalDate.now().minusDays(1));

        given(usersRepository.findById(CURRENT_USER_ID)).willReturn(Optional.empty());

        // when / then
        assertThrows(UsersNotFoundException.class, () -> memoirService.writeMemoir(request));
        verify(memoirRepository, never()).save(any());
    }

    @Test
    @DisplayName("회고 작성 서비스 - 중복 회고 예외")
    void writeMemoir_throwsWhenAlreadyExists() {
        // given
        Users users = new Users();
        ReflectionTestUtils.setField(users, "id", 1L);

        MemoirWriteRequest request = new MemoirWriteRequest();
        request.setData(Map.of("title", "새 회고", "content", "새 내용"));
        request.setDate(LocalDate.now().minusDays(1));

        Memoirs existing = new Memoirs();
        existing.setId(10L);

        given(usersRepository.findById(CURRENT_USER_ID)).willReturn(Optional.of(users));
        given(memoirRepository.findByUsersIdAndDate(CURRENT_USER_ID, request.getDate())).willReturn(Optional.of(existing));

        // when / then
        assertThrows(MemoirAlreadyExistException.class, () -> memoirService.writeMemoir(request));
        verify(memoirRepository, never()).save(any());
    }

    @Test
    @DisplayName("회고 작성 서비스 - 미래 날짜 예외")
    void writeMemoir_throwsWhenDateIsFuture() {
        // given
        Users users = new Users();
        ReflectionTestUtils.setField(users, "id", 1L);

        MemoirWriteRequest request = new MemoirWriteRequest();
        request.setData(Map.of("title", "새 회고", "content", "새 내용"));
        request.setDate(LocalDate.now().plusDays(1));

        given(usersRepository.findById(CURRENT_USER_ID)).willReturn(Optional.of(users));
        given(memoirRepository.findByUsersIdAndDate(CURRENT_USER_ID, request.getDate())).willReturn(Optional.empty());

        // when / then
        assertThrows(IllegalArgumentException.class, () -> memoirService.writeMemoir(request));
        verify(memoirRepository, never()).save(any());
    }

    @Test
    @DisplayName("개선점 수정 서비스 - 정상 케이스")
    void updateImprovement_updatesImprovementData() {
        // given
        Memoirs memoir = new Memoirs();
        memoir.setId(1L);
        memoir.setImprovement(Map.of("title", "기존 개선", "content", "기존 내용"));

        MemoirUpdateRequest request = new MemoirUpdateRequest();
        request.setData(Map.of("title", "수정된 개선", "content", "수정된 내용"));

        given(memoirRepository.findByIdAndUsersId(1L, CURRENT_USER_ID)).willReturn(Optional.of(memoir));

        // when
        memoirService.updateImprovement(request, 1L);

        // then
        assertEquals("수정된 개선", memoir.getImprovement().get("title"));
        assertEquals("수정된 내용", memoir.getImprovement().get("content"));
        verify(memoirRepository, never()).save(any());
    }

    @Test
    @DisplayName("개선점 수정 서비스 - 회고 없음 예외")
    void updateImprovement_throwsWhenNotFound() {
        // given
        MemoirUpdateRequest request = new MemoirUpdateRequest();
        request.setData(Map.of("title", "수정된 개선", "content", "수정된 내용"));

        given(memoirRepository.findByIdAndUsersId(1L, CURRENT_USER_ID)).willReturn(Optional.empty());

        // when / then
        assertThrows(MemoirNotFoundException.class, () -> memoirService.updateImprovement(request, 1L));
    }
}