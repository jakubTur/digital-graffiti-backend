
package edu.w04ist.si041g.backend.post;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostReadController {

    private final PostReadService postReadService;

    @PostMapping("/{postId}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long postId,
            Authentication authentication
    ) {
        Long userId = getUserId(authentication);

        postReadService.markAsRead(userId, postId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/read-ids")
    public Set<Long> getReadPostIds(Authentication authentication) {
        Long userId = getUserId(authentication);

        return postReadService.getReadPostIds(userId);
    }

    private Long getUserId(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof Long userId)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication required"
            );
        }

        return userId;
    }
}
