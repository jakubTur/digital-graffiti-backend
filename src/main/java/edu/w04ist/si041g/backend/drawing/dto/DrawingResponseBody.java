package edu.w04ist.si041g.backend.drawing.dto;

public record DrawingResponseBody (
        Long id,
        String username,
        Long postId,
        Long originId,
        String imageUrl
){}
