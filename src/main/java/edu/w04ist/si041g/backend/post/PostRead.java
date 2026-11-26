
package edu.w04ist.si041g.backend.post;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(
        name = "post_reads",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_post_reads_user_post",
                columnNames = {"user_id", "post_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class PostRead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "read_at", nullable = false, updatable = false)
    private Instant readAt;

    public PostRead(Long userId, Long postId) {
        this.userId = userId;
        this.postId = postId;
        this.readAt = Instant.now();
    }
}
