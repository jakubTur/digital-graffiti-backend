
package edu.w04ist.si041g.backend.post;
import edu.w04ist.si041g.backend.post.dto.PhotoResponseBody;

import edu.w04ist.si041g.backend.storage.SupabaseStorageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

@RestController
@RequestMapping("/api/posts")
public class PostPhotoController {

    private final PostRepository postRepository;
    private final JdbcTemplate jdbc;
    private final SupabaseStorageService storage;

    public PostPhotoController(
            PostRepository postRepository,
            JdbcTemplate jdbc,
            SupabaseStorageService storage
    ) {
        this.postRepository = postRepository;
        this.jdbc = jdbc;
        this.storage = storage;
    }

    @PostMapping(
            value = "/{postId}/photo",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public PhotoResponseBody uploadPhoto(
            @PathVariable Long postId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) throws IOException {

        if (authentication == null
                || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED
            );
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Post not found"
                ));

        Long currentUserId;
        try {
            currentUserId = Long.valueOf(authentication.getName());
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Authenticated user ID unavailable"
            );
        }

        if (!post.getUserId().getId().equals(currentUserId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You do not own this post"
            );
        }

        if (post.getImagePath() != null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Post already has a photo"
            );
        }

        String path = storage.upload(postId, file);

        try {
            int updated = jdbc.update("""
                    UPDATE public.posts_tmp
                    SET image_path = ?
                    WHERE id = ?
                      AND poster_id = ?
                      AND image_path IS NULL
                    """, path, postId, currentUserId);

            if (updated != 1) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Post changed during upload"
                );
            }
        } catch (RuntimeException ex) {
            try {
                storage.delete(path);
            } catch (RuntimeException cleanupError) {
                ex.addSuppressed(cleanupError);
            }
            throw ex;
        }

        return new PhotoResponseBody(path);
    }

}