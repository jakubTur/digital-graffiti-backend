
package edu.w04ist.si041g.backend.common.image;
import edu.w04ist.si041g.backend.drawing.Drawing;
import edu.w04ist.si041g.backend.drawing.DrawingRepository;
import edu.w04ist.si041g.backend.post.Post;
import edu.w04ist.si041g.backend.post.PostRepository;
import edu.w04ist.si041g.backend.common.image.dto.PhotoResponseBody;

import edu.w04ist.si041g.backend.storage.SupabaseStorageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import edu.w04ist.si041g.backend.common.image.dto.PhotoUrlResponseBody;

import java.io.IOException;

@RestController
@RequestMapping("/api/posts")
public class PostPhotoController {

    private final PostRepository postRepository;
    private final JdbcTemplate jdbc;
    private final SupabaseStorageService storage;
    private final DrawingRepository drawingRepository;

    public PostPhotoController(
            PostRepository postRepository,
            JdbcTemplate jdbc,
            SupabaseStorageService storage,
            DrawingRepository drawingRepository
    ) {
        this.postRepository = postRepository;
        this.jdbc = jdbc;
        this.storage = storage;
        this.drawingRepository = drawingRepository;
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
        System.out.println("authentication name: " + authentication.getName());


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
        System.out.println("post owner id: " + post.getUserId().getId());
        System.out.println("requested post id: " + postId);
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

        if (post.getBackgroundImagePath() != null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Post already has a photo"
            );
        }

        String path = storage.upload("posts", postId, file);

        try {
            int updated = jdbc.update("""
                    UPDATE public.posts_new_tmp_final
                    SET background_image_path = ?
                    WHERE id = ?
                      AND poster_id = ?
                      AND background_image_path IS NULL
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

    @PostMapping(
            value = "/drawing/{drawingId}/photo",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public PhotoResponseBody drawPhoto(
            @PathVariable Long drawingId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) throws IOException {
        if (authentication == null
                || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED
            );
        }

        Drawing drawing = drawingRepository.findById(drawingId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Drawing not found"
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

        if (!drawing.getUserId().getId().equals(currentUserId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You do not own this post"
            );
        }

        if (drawing.getImagePath() != null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Drawing is already present"
            );
        }

        String path = storage.upload("drawings", drawingId, file);

        try {
            int updated = jdbc.update("""
                    UPDATE public.drawings_new_tmp_final
                    SET image_path = ?
                    WHERE id = ?
                      AND poster_id = ?
                      AND image_path IS NULL
                    """, path, drawingId, currentUserId);

            if (updated != 1) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Drawing changed during upload"
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

    @GetMapping("/{postId}/photo-url")
    public PhotoUrlResponseBody getPhotoUrl(
            @PathVariable Long postId
    ) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Post not found"
                ));

        if (post.getBackgroundImagePath() == null
                || post.getBackgroundImagePath().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Post has no photo"
            );
        }

        String url = storage.createSignedUrl(
                post.getBackgroundImagePath()
        );

        return new PhotoUrlResponseBody(url);
    }
}