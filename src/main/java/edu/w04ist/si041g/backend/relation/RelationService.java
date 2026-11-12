package edu.w04ist.si041g.backend.relation;

import edu.w04ist.si041g.backend.relation.dto.RelationRequest;
import edu.w04ist.si041g.backend.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.management.relation.RelationType;
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
        return relationRepository.save(newRelation);
    }

    public List<Relation> getRelationsOf(Long id, String status) {
        RelationStatus relationStatus;
        try {
            relationStatus = RelationStatus.valueOf(status.toLowerCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid relation status: " + status);
        }
        if (relationStatus == RelationStatus.accepted) {
            return relationRepository.findRelationsByUserIdAndStatus(id, relationStatus);
        }
        return relationRepository.findAllBySender_IdAndStatus(id, relationStatus);
    }
    public Relation getRelationBetween(Long firstId, Long secondId) {
        return relationRepository.findByInviterAndInvitee(firstId, secondId)
                .orElse(null);
    }
}
