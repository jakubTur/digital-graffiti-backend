package edu.w04ist.si041g.backend.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
@Builder
public class User{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String nickname;
    @Column(nullable = false)
    private boolean active = true; // not banned
    @Column(nullable = false)
    private boolean verified = false; // email verification status
    @Column(unique = true, nullable = false)
    private String email;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(
            name = "role",
            columnDefinition = "public.user_role",
            nullable = false
    )
    private UserRole role = UserRole.user;
}