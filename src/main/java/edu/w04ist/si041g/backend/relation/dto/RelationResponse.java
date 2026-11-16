package edu.w04ist.si041g.backend.relation.dto;

public record RelationResponse (
    String senderNickname,
    String receiverNickname,
    String status,
    boolean blocked
) {}
