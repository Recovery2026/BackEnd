package com.example.recovery.service.memoirs;

import com.example.recovery.common.exception.MemoirAlreadyExistException;
import com.example.recovery.common.exception.MemoirNotFoundException;
import com.example.recovery.common.exception.UsersNotFoundException;
import com.example.recovery.domain.memoirs.Memoirs;
import com.example.recovery.domain.user.Users;
import com.example.recovery.dto.MemoirSimple;
import com.example.recovery.repository.memoirs.MemoirRepository;
import com.example.recovery.repository.users.UsersRepository;
import com.example.recovery.request.*;
import com.example.recovery.response.MemoirCalenderResponse;
import com.example.recovery.response.MemoirResponse;
import com.example.recovery.response.MemoirSimpleResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class MemoirService {

    private final MemoirRepository memoirRepository;
    private final UsersRepository usersRepository;

    @Transactional(readOnly = true)
    public MemoirSimpleResponse getMemoirList(MemoirBodyRequest request, SimplePageRequest simplePageRequest) {
        List<Memoirs> memoirs = memoirRepository.getMemoirsByRequest(request, simplePageRequest);

        List<MemoirSimple> memoirList = memoirs.stream()
                .map(memoir -> MemoirSimple.builder()
                        .id(memoir.getId())
                        .memoir(memoir.getMemoir())
                        .date(memoir.getDate())
                        .build())
                .toList();

        return new MemoirSimpleResponse(memoirList, memoirs.size());
    }

    @Transactional(readOnly = true)
    public MemoirCalenderResponse getMemoirCalender(MemoirBodyRequest request, MemoirCalenderRequest simplePageRequest) {
        Memoirs memoirs = memoirRepository.findByUsersIdAndDate(request.getUserId(), simplePageRequest.getDate())
                .orElseThrow(() -> new MemoirNotFoundException("해당 날짜의 회고가 없습니다."));

        return new MemoirCalenderResponse(
                memoirs.getId(),
                memoirs.getMemoir(),
                memoirs.getDate()
        );
    }

    @Transactional(readOnly = true)
    public MemoirResponse getMemoir(MemoirBodyRequest request, Long memoirId) {
        Memoirs memoirs = memoirRepository.findByIdAndUsersId(memoirId, request.getUserId())
                .orElseThrow(() -> new MemoirNotFoundException("해당 회고가 없습니다."));

        return MemoirResponse.builder()
                .memoir(memoirs.getMemoir())
                .improvement(memoirs.getImprovement())
                .feedback(memoirs.getFeedback())
                .build();
    }

    @Transactional
    public void updateMemoir(MemoirUpdateRequest request, Long memoirId) {
        Memoirs memoirs = memoirRepository.findByIdAndUsersId(memoirId, request.getUserId())
                .orElseThrow(() -> new MemoirNotFoundException("해당 회고가 없습니다."));

        memoirs.setMemoir(request.getData());
    }

    @Transactional
    public void writeMemoir(MemoirWriteRequest request) {
        Users users = usersRepository.findById(request.getUserId())
                .orElseThrow(() -> new UsersNotFoundException("해당 사용자가 없습니다."));

        if (memoirRepository.findByUsersIdAndDate(request.getUserId(), request.getDate()).isPresent()) {
            throw new MemoirAlreadyExistException("해당 날짜의 회고가 이미 존재합니다.");
        }

        if (request.getDate().isAfter(java.time.LocalDate.now())) {
            throw new IllegalArgumentException("회고 날짜는 오늘 이전이어야 합니다.");
        }

        Memoirs memoirs = new Memoirs();
        memoirs.setUsers(users);
        memoirs.setMemoir(request.getData());
        memoirs.setDate(request.getDate());

        memoirRepository.save(memoirs);
    }

    @Transactional
    public void updateImprovement(MemoirUpdateRequest request, Long memoirId) {
        Memoirs memoirs = memoirRepository.findByIdAndUsersId(memoirId, request.getUserId())
                .orElseThrow(() -> new MemoirNotFoundException("해당 회고가 없습니다."));

        memoirs.setImprovement(request.getData());
    }
}
