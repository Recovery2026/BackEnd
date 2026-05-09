package com.example.recovery.maker;

import com.example.recovery.domain.user.Users;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;

public class UsersMaker {

    private final TestEntityManager entityManager;

    public UsersMaker(TestEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Users persist(String nickname, OffsetDateTime createdAt) {
        Users user = createUser(nickname, createdAt);
        entityManager.persist(user);
        entityManager.flush();
        return entityManager.find(Users.class, extractId(user));
    }

    public long extractId(Users user) {
        return (long) entityManager.getEntityManager()
                .getEntityManagerFactory()
                .getPersistenceUnitUtil()
                .getIdentifier(user);
    }

    private Users createUser(String nickname, OffsetDateTime createdAt) {
        Users user = new Users();
        ReflectionTestUtils.setField(user, "nickname", nickname);
        ReflectionTestUtils.setField(user, "profileUrl", null);
        ReflectionTestUtils.setField(user, "createdAt", createdAt);
        ReflectionTestUtils.setField(user, "activation", true);
        return user;
    }
}


