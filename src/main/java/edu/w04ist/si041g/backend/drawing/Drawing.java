package edu.w04ist.si041g.backend.drawing;

import edu.w04ist.si041g.backend.post.Post;
import edu.w04ist.si041g.backend.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "drawings_new_tmp_final")
public class Drawing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name="poster_id", nullable = false)
    private User userId;
    @ManyToOne
    @JoinColumn(name="origin_id", nullable = true)
    private Drawing origin;
    @ManyToOne
    @JoinColumn(name="post_id", nullable = false)
    private Post post;
    @Column(name = "image_path")
    private String imagePath;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
