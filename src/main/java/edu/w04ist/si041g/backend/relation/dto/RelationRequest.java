package edu.w04ist.si041g.backend.relation.dto;

import lombok.Data;

@Data
public class RelationRequest {
    private Long senderId;
    private Long receiverId;
    private String type;
}
