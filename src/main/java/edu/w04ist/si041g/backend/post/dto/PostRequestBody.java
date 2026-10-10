package edu.w04ist.si041g.backend.post.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class PostRequestBody {
    private String title;
    private String text;
    private String textColor;
    private String username;
    private Double latitude;
    private Double longitude;
}
