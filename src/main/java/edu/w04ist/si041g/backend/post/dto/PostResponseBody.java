package edu.w04ist.si041g.backend.post.dto;


import java.time.Instant;

public record PostResponseBody (
    Long id,
    Instant postedAt,
    String username,
    String imageUrl,
    Double latitude,
    Double longitude,
    boolean active
){}
