package edu.w04ist.si041g.backend.notification;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import edu.w04ist.si041g.backend.notification.dto.NotificationResponse;
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
    public ResponseEntity<List<NotificationResponse>> getAllNotifications(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        List<QueryDocumentSnapshot> notifications = notificationStoreService.getNotifications(userId);
        return ResponseEntity.ok(notifications.stream().map(this::toNotificationResponse).toList());
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Void> markNotificationAsRead(@PathVariable String notificationId, Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        notificationStoreService.markAsRead(userId, notificationId);
        return ResponseEntity.ok().build();
    }

    private NotificationResponse toNotificationResponse(QueryDocumentSnapshot notification) {
        String referenceId = notification.getString("referenceId");
        Timestamp createdAt = notification.getTimestamp("createdAt");
        return new NotificationResponse(
                notification.getId(),
                notification.getString("title"),
                notification.getString("body"),
                notification.getString("type"),
                referenceId == null ? null : Long.valueOf(referenceId),
                Boolean.TRUE.equals(notification.getBoolean("isRead")),
                createdAt == null ? null : createdAt.toDate().toInstant()
        );
    }
}
