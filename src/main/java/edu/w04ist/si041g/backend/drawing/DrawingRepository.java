package edu.w04ist.si041g.backend.drawing;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DrawingRepository extends JpaRepository<Drawing,Long> {
    List<Drawing> findByPostId(Long postId);
}
