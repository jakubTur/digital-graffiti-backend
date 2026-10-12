package edu.w04ist.si041g.backend.post.dto;


import java.time.Instant;

public record PostResponseBody (
    Long id,
    String title,
    String text,
    Instant postedAt,
    String textColor,
    String username,
    Double latitude,
    Double longitude
){}
