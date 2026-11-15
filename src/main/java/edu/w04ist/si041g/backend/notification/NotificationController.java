package edu.w04ist.si041g.backend.notification;

import com.google.cloud.firestore.QueryDocumentSnapshot;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationStoreService notificationStoreService;

    public NotificationController(NotificationStoreService notificationStoreService) {
        this.notificationStoreService = notificationStoreService;
    }

    @GetMapping
    public ResponseEntity<List<QueryDocumentSnapshot>> getAllNotifications(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        List<QueryDocumentSnapshot> notifications = notificationStoreService.getNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Void> markNotificationAsRead(@PathVariable String notificationId, Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        notificationStoreService.markAsRead(userId, notificationId);
        return ResponseEntity.ok().build();
    }
}
