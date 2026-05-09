package com.example.recovery.domain.memoirs;

import com.example.recovery.domain.user.Users;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.Map;

@Getter
@Setter
@Entity
@Table(name = "memoirs")
public class Memoirs {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "memoir_id", nullable = false)
    private Long id;

    @NotFound(action = NotFoundAction.EXCEPTION)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", insertable = true, updatable = true, nullable = false)
    private Users users;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "memoir")
    private Map<String, Object> memoir;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "improvement")
    private Map<String, Object> improvement;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "feedback")
    private Map<String, Object> feedback;

    @Column(name = "date", nullable = false)
    private LocalDate date;

}