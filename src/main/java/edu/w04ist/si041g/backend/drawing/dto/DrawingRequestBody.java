package edu.w04ist.si041g.backend.drawing.dto;

import lombok.Data;

@Data
public class DrawingRequestBody {
    private Long userId;
    private Long postId;
    private Long originId;
}
