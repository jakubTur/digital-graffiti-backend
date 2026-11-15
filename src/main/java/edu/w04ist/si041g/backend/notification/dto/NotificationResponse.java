package edu.w04ist.si041g.backend.notification.dto;

import java.time.Instant;

public record NotificationResponse(
        String id,
        String title,
        String body,
        String type,
        Long referenceId,
        boolean isRead,
        Instant createdAt
) {}
