package com.example.recovery.repository.users;

import com.example.recovery.domain.user.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.Optional;

public interface UsersRepository extends JpaRepository<Users, Long>, QuerydslPredicateExecutor<Users>, UsersRepositoryCustom {
    Optional<Users> findById(Long id);
}
