package com.example.recovery.domain.oauth2;

import com.example.recovery.domain.user.Users;
import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import java.time.OffsetDateTime;

@Entity
@Table(name = "user_oauth2_connections")
public class UserOauth2Connection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "connection_id", nullable = false)
    private Long id;

    @NotFound(action = NotFoundAction.EXCEPTION)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", insertable = true, updatable = true, nullable = false)
    private Users users;

    @NotFound(action = NotFoundAction.EXCEPTION)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", referencedColumnName = "provider_id", insertable = true, updatable = true, nullable = false)
    private Oauth2Provider provider;

    @Column(name = "oauth2_user_id", nullable = false, length = 255)
    private String oauth2UserId;

    @ColumnDefault("now()")
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

}