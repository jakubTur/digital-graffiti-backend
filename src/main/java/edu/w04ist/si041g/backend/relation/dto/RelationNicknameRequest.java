package edu.w04ist.si041g.backend.relation.dto;

import lombok.Data;

@Data
public class RelationNicknameRequest {
    private String senderNickname;
    private String receiverNickname;
    private String type;
}
