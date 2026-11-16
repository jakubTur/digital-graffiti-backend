package edu.w04ist.si041g.backend.post;

import edu.w04ist.si041g.backend.drawing.DrawingController;
import edu.w04ist.si041g.backend.notification.FcmService;
import edu.w04ist.si041g.backend.notification.NotificationStoreService;
import edu.w04ist.si041g.backend.post.dto.PostRequestBody;
import edu.w04ist.si041g.backend.post.dto.PostResponseBody;
import edu.w04ist.si041g.backend.relation.RelationService;
import edu.w04ist.si041g.backend.storage.SupabaseStorageService;
import edu.w04ist.si041g.backend.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
public class PostController {
    private final PostService postService;
    private final SupabaseStorageService storage;
    private final RelationService relationService;
    private final NotificationStoreService notificationStoreService;
    private final FcmService fcmService;
    private static final Logger logger = LoggerFactory.getLogger(DrawingController.class);
    public PostController(PostService postService, SupabaseStorageService storage, RelationService relationService, NotificationStoreService notificationStoreService, FcmService fcmService) {
        this.storage = storage;
        this.postService = postService;
        this.relationService = relationService;
        this.notificationStoreService = notificationStoreService;
        this.fcmService = fcmService;
    }

    @PostMapping("/create")
    public ResponseEntity<Map<String, Long>> createPost(@RequestBody PostRequestBody post, Authentication authentication) {
        if (!Long.valueOf(authentication.getName()).equals(post.getUserId())) {
            return ResponseEntity.status(403).build();
        }
        Post createdPost = postService.createPost(post);
        URI location = URI.create("/api/posts/" + createdPost.getId());

        List<User> friends = relationService.getFriendsOf(createdPost.getUserId().getId());
        for (User friend : friends) {
            try {
                notificationStoreService.createNotification(
                        friend.getId(),
                        createdPost.getUserId().getNickname() + " has created a new post!",
                        "Open the app to check it out!",
                        "NEW_POST",
                        createdPost.getId().toString()
                );
            } catch (Exception exception) {
                logger.warn(
                        "Could not create post notification for user {}",
                        friend.getId(),
                        exception
                );
            }
            String friendToken = friend.getFirebaseToken();
            if (friendToken != null && !friendToken.isBlank()) {
                try {
                    fcmService.sendNotification(
                            friendToken,
                            createdPost.getUserId().getNickname() + " has created a new post!",
                            "Open the app to check it out!",
                            "NEW_POST",
                            createdPost.getId().toString()
                    );
                } catch (Exception exception) {
                    logger.warn(
                            "Could not send FCM notification to user {}",
                            friend.getId(),
                            exception
                    );
                }
            }
        }

        return ResponseEntity
                .created(location)
                .body(Map.of("id", createdPost.getId()));
    }

    @GetMapping("/all")
    public ResponseEntity<List<PostResponseBody>> getAllPosts() {
        return ResponseEntity.ok(postService.getAllPosts().stream().map(this::toPostResponseBody).toList());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id, Authentication authentication) {
        Post post = postService.getPostById(id);
        if (!Long.valueOf(authentication.getName()).equals(post.getUserId().getId())) {
            return ResponseEntity.status(403).build();
        }
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

    private PostResponseBody toPostResponseBody(Post post) {
        String imageUrl = post.getBackgroundImagePath() == null
                ? null
                : storage.createSignedUrl(post.getBackgroundImagePath());
        return new PostResponseBody(
                post.getId(),
                post.getPostedAt(),
                post.getUserId().getNickname(),
                imageUrl,
                post.getLocation().getCoordinate().getY(),
                post.getLocation().getCoordinate().getX()
        );
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<PostResponseBody>> getNearbyPosts(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "1000") double radiusMeters
    ) {
        List<PostResponseBody> posts = postService
                .getNearbyPosts(latitude, longitude, radiusMeters)
                .stream()
                .map(this::toPostResponseBody)
                .toList();

        return ResponseEntity.ok(posts);
    }

}
