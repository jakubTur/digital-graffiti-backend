package edu.w04ist.si041g.backend.notification;

import com.google.cloud.firestore.*;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
public class NotificationStoreService {
    private final Firestore firestore;

    public NotificationStoreService(Firestore firestore) {
        this.firestore = firestore;
    }
    public void createNotification(Long recipientId, String title, String body, String type, String referenceId){
        String notificationId = UUID.randomUUID().toString();
        Map<String,Object> notification = new HashMap<>();
        notification.put("title", title);
        notification.put("body", body);
        notification.put("type", type);
        notification.put("referenceId", referenceId);
        notification.put("isRead", false);
        notification.put("createdAt", FieldValue.serverTimestamp());
        DocumentReference document = firestore
                .collection("notifications")
                .document(String.valueOf(recipientId))
                .collection("userNotifications")
                .document(notificationId);
        try {
            document.set(notification).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Saving was interrupted",e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Saving was failed",e);
        }
    }
    public List<QueryDocumentSnapshot> getNotifications(Long userId) {
        try {
            return firestore
                    .collection("notifications")
                    .document(String.valueOf(userId))
                    .collection("userNotifications")
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .get()
                    .get()
                    .getDocuments();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Fetching notifications was interrupted",e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Fetching notifications was failed",e);
        }
    }
    public void markAsRead(Long userId, String notificationId){
        try {
            firestore
                    .collection("notifications")
                    .document(String.valueOf(userId))
                    .collection("userNotifications")
                    .document(notificationId)
                    .update("isRead", true)
                    .get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Marking as read was interrupted",e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Marking as read was failed",e);
        }
    }
    public void deleteNotification(Long userId, String notificationId){
        firestore
                .collection("notifications")
                .document(String.valueOf(userId))
                .collection("userNotifications")
                .document(notificationId)
                .delete();
    }
}

