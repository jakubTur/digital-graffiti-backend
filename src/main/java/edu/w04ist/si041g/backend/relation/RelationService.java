package edu.w04ist.si041g.backend.relation;

import edu.w04ist.si041g.backend.relation.dto.RelationNicknameRequest;
import edu.w04ist.si041g.backend.relation.dto.RelationRequest;
import edu.w04ist.si041g.backend.user.User;
import edu.w04ist.si041g.backend.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class RelationService {
    @Autowired
    private RelationRepository relationRepository;
    @Autowired
    private UserRepository userRepository;
    public RelationService() {}

    public Relation createRelation(RelationRequest relation) {
        Relation newRelation = new Relation();
        newRelation.setSender(userRepository.findById(relation.getSenderId()).orElseThrow(() -> new RuntimeException("Sender user not found")));
        newRelation.setReceiver(userRepository.findById(relation.getReceiverId()).orElseThrow(() -> new RuntimeException("Receiver user not found")));
        newRelation.setStatus(relation.getType().equals("block") ? RelationStatus.blocked : RelationStatus.pending);
        newRelation.setCreatedAt(Instant.now());
        newRelation.setUpdatedAt(Instant.now());
        newRelation.setBlocked(relation.getType().equals("block"));
        return relationRepository.save(newRelation);
    }

    public Relation createRelationByNicknames(RelationNicknameRequest relation) {
        Relation newRelation = new Relation();
        newRelation.setSender(userRepository.findByNickname(relation.getSenderNickname()).orElseThrow(() -> new RuntimeException("Sender user not found")));
        newRelation.setReceiver(userRepository.findByNickname(relation.getReceiverNickname()).orElseThrow(() -> new RuntimeException("Receiver user not found")));
        newRelation.setStatus(relation.getType().equals("block") ? RelationStatus.blocked : RelationStatus.pending);
        newRelation.setCreatedAt(Instant.now());
        newRelation.setUpdatedAt(Instant.now());
        newRelation.setBlocked(relation.getType().equals("block"));
        return relationRepository.save(newRelation);
    }

    public void unblock(Long id, String nickname) {
        Relation relation = relationRepository.findBySender_IdAndReceiver_Nickname(id, nickname)
                .orElseThrow(() -> new RuntimeException("Relation not found"));
        relation.setBlocked(false);
        relation.setStatus(RelationStatus.declined);
        relationRepository.save(relation);
    }

    public void deleteRelation(String firstNickname, String secondNickname) {
        Relation relation = relationRepository.findByInviterAndInviteeNicknames(firstNickname, secondNickname)
                .orElseThrow(() -> new RuntimeException("Relation not found"));
        relationRepository.delete(relation);
    }

    public Relation updateRelation(RelationNicknameRequest request) {
        Relation relation = relationRepository.findByInviterAndInviteeNicknames(request.getSenderNickname(), request.getReceiverNickname())
                .orElseThrow(() -> new RuntimeException("Relation not found"));
        if (request.getType().toLowerCase().equals("accepted") && relation.getStatus() != RelationStatus.pending) {
            throw new RuntimeException("Cannot accept a relation that is not pending");
        }
        relation.setBlocked(request.getType().equals("block"));
        relation.setStatus(RelationStatus.valueOf(request.getType().toLowerCase()));
        relation.setReceiver(userRepository.findByNickname(request.getReceiverNickname()).orElseThrow(() -> new RuntimeException("Receiver user not found")));
        relation.setSender(userRepository.findByNickname(request.getSenderNickname()).orElseThrow(() -> new RuntimeException("Sender user not found")));
        relation.setUpdatedAt(Instant.now());
        return relationRepository.save(relation);
    }

    public List<Relation> getRelationsOf(Long id, String status) {
        RelationStatus relationStatus;
        try {
            relationStatus = RelationStatus.valueOf(status.toLowerCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid relation status: " + status);
        }
        return relationRepository.findRelationsByUserIdAndStatus(id, relationStatus);
    }
    public Relation getRelationBetween(Long firstId, Long secondId) {
        return relationRepository.findByInviterAndInvitee(firstId, secondId)
                .orElse(null);
    }

    public List<User> getFriendsOf(Long userId) {
        List<Long> friendIds = relationRepository.findFriendsOf(userId);
        return userRepository.findAllById(friendIds);
    }

    public Relation getRelationBetweenNicknames(String firstNickname, String secondNickname) {
        return relationRepository.findByInviterAndInviteeNicknames(firstNickname, secondNickname)
                .orElse(null);
    }
}
