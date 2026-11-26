
package edu.w04ist.si041g.backend.post;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostReadRepository extends JpaRepository<PostRead, Long> {

    boolean existsByUserIdAndPostId(Long userId, Long postId);

    List<PostRead> findByUserId(Long userId);
}
