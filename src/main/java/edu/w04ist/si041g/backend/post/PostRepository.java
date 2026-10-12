package edu.w04ist.si041g.backend.post;

import edu.w04ist.si041g.backend.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post,Long> {

    void deleteAllByUserId_Id(Long userId);
}
