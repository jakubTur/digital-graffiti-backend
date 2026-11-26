
package edu.w04ist.si041g.backend.post;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostReadService {

    private final PostReadRepository postReadRepository;

    @Transactional
    public void markAsRead(Long userId, Long postId) {
        boolean alreadyRead =
                postReadRepository.existsByUserIdAndPostId(userId, postId);

        if (!alreadyRead) {
            postReadRepository.save(new PostRead(userId, postId));
        }
    }

    @Transactional(readOnly = true)
    public Set<Long> getReadPostIds(Long userId) {
        return postReadRepository.findByUserId(userId)
                .stream()
                .map(PostRead::getPostId)
                .collect(Collectors.toSet());
    }
}
