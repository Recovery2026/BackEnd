package com.example.recovery.repository.memoirs;

import com.example.recovery.domain.memoirs.Memoirs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.time.LocalDate;
import java.util.Optional;

public interface MemoirRepository extends JpaRepository<Memoirs, Long>, QuerydslPredicateExecutor<Memoirs>, MemoirRepositoryCustom {
    Optional<Memoirs> findByUsersIdAndDate(Long userId, LocalDate date);

    Optional<Memoirs> findByIdAndUsersId(Long Id, Long userId);
}
