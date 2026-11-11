package edu.w04ist.si041g.backend.drawing.dto;

import java.time.Instant;

public record DrawingResponseBody (
        Long id,
        String username,
        Long postId,
        Long originId,
        String imageUrl,
        Instant createdAt
){}
