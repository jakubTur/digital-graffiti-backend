package edu.w04ist.si041g.backend.post;

import edu.w04ist.si041g.backend.user.User;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

import java.time.Instant;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "posts_new_tmp_final")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Instant postedAt;
    @ManyToOne
    @JoinColumn(name="poster_id", nullable = false)
    private User userId;
    @Column(name = "location", columnDefinition = "extensions.geography(Point, 4326)")
    private Point location;
    @Column(name = "background_image_path")
    private String backgroundImagePath;
}
