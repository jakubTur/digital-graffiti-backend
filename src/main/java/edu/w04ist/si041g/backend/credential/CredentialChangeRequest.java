package edu.w04ist.si041g.backend.credential;

import edu.w04ist.si041g.backend.user.User;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "credential_change_tokens")
@Data
public class CredentialChangeRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(nullable = false)
    private boolean passwordChange;
    @Column(nullable = false)
    private String value;
    @Column(nullable = false, unique = true)
    private String token;
    private LocalDateTime expiresAt;
}
