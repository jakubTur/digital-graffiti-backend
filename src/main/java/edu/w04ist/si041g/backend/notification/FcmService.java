package edu.w04ist.si041g.backend.notification;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import org.springframework.stereotype.Service;

import com.google.firebase.messaging.Notification;

@Service
public class FcmService {
    public String sendNotification(String targetToken, String title, String body, String type, String referenceId) throws Exception {
        Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();
        Message message = Message.builder()
                .setToken(targetToken)
                .setNotification(notification)
                .putData("type", type)
                .putData("referenceId", referenceId)
                .build();
        return FirebaseMessaging.getInstance().send(message);
    }
}
