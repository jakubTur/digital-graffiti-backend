package edu.w04ist.si041g.backend.relation;

import edu.w04ist.si041g.backend.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_relations_new")
@Builder
public class Relation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name="inviter", nullable = false)
    private User sender;
    @ManyToOne
    @JoinColumn(name="invitee", nullable = false)
    private User receiver;
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status",
            columnDefinition = "public.relation_status",
            nullable = false)
    private RelationStatus status;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at")
    private Instant updatedAt;
    @Column(name = "blocked", nullable = false)
    private boolean blocked = false;
}
