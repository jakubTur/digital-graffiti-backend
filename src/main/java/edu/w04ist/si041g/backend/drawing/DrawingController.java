package edu.w04ist.si041g.backend.drawing;

import com.google.firebase.messaging.FirebaseMessagingException;
import edu.w04ist.si041g.backend.drawing.dto.DrawingRequestBody;
import edu.w04ist.si041g.backend.drawing.dto.DrawingResponseBody;
import edu.w04ist.si041g.backend.notification.FcmService;
import edu.w04ist.si041g.backend.notification.NotificationStoreService;
import edu.w04ist.si041g.backend.storage.SupabaseStorageService;
import edu.w04ist.si041g.backend.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts/drawings")
public class DrawingController {
    private final DrawingService drawingService;
    private final SupabaseStorageService storage;
    private final FcmService fcmService;
    private final NotificationStoreService notificationStoreService;
    private static final Logger logger = LoggerFactory.getLogger(DrawingController.class);
    public DrawingController(DrawingService drawingService, SupabaseStorageService storage, FcmService fcmService, NotificationStoreService notificationStoreService) {
        this.storage = storage;
        this.drawingService = drawingService;
        this.fcmService = fcmService;
        this.notificationStoreService = notificationStoreService;
    }

    @PostMapping("/add")
    public ResponseEntity<DrawingResponseBody> addDrawing(@RequestBody DrawingRequestBody drawingRequestBody, Authentication authentication) {
        if (!Long.valueOf(authentication.getName()).equals(drawingRequestBody.getUserId())) {
            return ResponseEntity.status(403).build();
        }
        Drawing createdDrawing = drawingService.addDrawing(drawingRequestBody);

        User receiver = createdDrawing.getOrigin() != null ?
                createdDrawing.getOrigin().getUserId() :
                createdDrawing.getPost().getUserId();
        if (!createdDrawing.getUserId().getId().equals(receiver.getId())) {
            try {
                notificationStoreService.createNotification(
                        receiver.getId(),
                        createdDrawing.getUserId().getNickname() + " has vandalized your drawing!",
                        "Open the app to thank them!",
                        "NEW_COMMENT",
                        createdDrawing.getId().toString()
                );
            } catch (Exception exception) {
                logger.warn(
                        "Could not create drawing notification for user {}",
                        receiver.getId(),
                        exception
                );
            }
            String receiverToken = receiver.getFirebaseToken();
            if (receiverToken != null && !receiverToken.isBlank()) {
                try {
                    fcmService.sendNotification(
                            receiverToken,
                            createdDrawing.getUserId().getNickname() + " has vandalized your drawing!",
                            "Open the app to thank them!",
                            "NEW_COMMENT",
                            createdDrawing.getId().toString()
                    );
                } catch (FirebaseMessagingException exception) {
                    logger.warn(
                            "Could not send drawing notification to user {}",
                            receiver.getId(),
                            exception
                    );
                }
            }
        }
        return ResponseEntity.ok(toDrawingResponseBody(createdDrawing));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<List<DrawingResponseBody>> getDrawingByPostId(@PathVariable Long postId) {
        return ResponseEntity.ok(drawingService.getDrawingByPostId(postId).stream().map(this::toDrawingResponseBody).toList());
    }

    private DrawingResponseBody toDrawingResponseBody(Drawing drawing) {
        String imageUrl = drawing.getImagePath() == null
                ? null
                : storage.createSignedUrl(drawing.getImagePath());
        return new DrawingResponseBody(
                drawing.getId(),
                drawing.getUserId().getNickname(),
                drawing.getPost().getId(),
                drawing.getOrigin() == null
                        ? null
                        : drawing.getOrigin().getId(),
                imageUrl,
                drawing.getCreatedAt()
        );
    }
}
