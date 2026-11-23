package edu.w04ist.si041g.backend.relation;

import edu.w04ist.si041g.backend.notification.FcmService;
import edu.w04ist.si041g.backend.notification.NotificationStoreService;
import edu.w04ist.si041g.backend.relation.dto.RelationNicknameRequest;
import edu.w04ist.si041g.backend.relation.dto.RelationRequest;
import edu.w04ist.si041g.backend.relation.dto.RelationResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/relations")
public class RelationController {
    private final RelationService relationService;
    private final NotificationStoreService notificationStoreService;
    private final FcmService fcmService;

    public RelationController(RelationService relationService, JdbcTemplate jdbc, NotificationStoreService notificationStoreService, FcmService fcmService) {
        this.relationService = relationService;
        this.notificationStoreService = notificationStoreService;
        this.fcmService = fcmService;
    }

    @PostMapping
    public ResponseEntity<Relation> createRelation(@RequestBody RelationRequest relationRequest, Authentication authentication) {
        if (!Long.valueOf(authentication.getName()).equals(relationRequest.getSenderId())) {
            return ResponseEntity.status(403).build();
        }
        Relation relation = relationService.createRelation(relationRequest);

        return ResponseEntity.created(URI.create("/relations/" + relation.getId())).build();
    }

    @PostMapping("/nicknames")
    public ResponseEntity<Relation> createRelationByNicknames(@RequestBody RelationNicknameRequest relationRequest, Authentication authentication) {
        Relation relation = relationService.createRelationByNicknames(relationRequest);
        if (!Long.valueOf(authentication.getName()).equals(relation.getSender().getId())) {
            return ResponseEntity.status(403).build();
        }
        if (relation.getStatus() == RelationStatus.pending){
            try {
                notificationStoreService.createNotification(
                        relation.getReceiver().getId(),
                        relation.getSender().getNickname() + " has sent you a friend request!",
                        "Open the app to accept or decline it.",
                        "NEW_FRIEND_REQUEST",
                        relation.getId().toString()
                );
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            String token = relation.getReceiver().getFirebaseToken();
            if (token != null && !token.isBlank()) {
                try {
                    fcmService.sendNotification(
                            token,
                            relation.getSender().getNickname() + " has sent you a friend request!",
                            "Open the app to accept or decline it.",
                            "NEW_FRIEND_REQUEST",
                            relation.getId().toString()
                    );
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return ResponseEntity.created(URI.create("/relations/" + relation.getId())).build();
    }

    @PutMapping
    public ResponseEntity<Relation> updateRelation(@RequestBody RelationNicknameRequest request, Authentication authentication) {
        Relation updatedRelation = relationService.updateRelation(request);
        if (!Long.valueOf(authentication.getName()).equals(updatedRelation.getSender().getId()) &&
        !Long.valueOf(authentication.getName()).equals(updatedRelation.getReceiver().getId())) {
            return ResponseEntity.status(403).build();
        }
        if (updatedRelation.getStatus() == RelationStatus.accepted) {
            try {
                notificationStoreService.createNotification(
                        updatedRelation.getSender().getId(),
                        updatedRelation.getReceiver().getNickname() + " has accepted your friend request!",
                        "Open the app to start chatting.",
                        "FRIEND_REQUEST_ACCEPTED",
                        updatedRelation.getId().toString()
                );
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            String token = updatedRelation.getSender().getFirebaseToken();
            if (token != null && !token.isBlank()) {
                try {
                    fcmService.sendNotification(
                            token,
                            updatedRelation.getReceiver().getNickname() + " has accepted your friend request!",
                            "Open the app to see where they paint.",
                            "FRIEND_REQUEST_ACCEPTED",
                            updatedRelation.getId().toString()
                    );
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return ResponseEntity.ok(updatedRelation);
    }

    @DeleteMapping("/{firstNickname}/{secondNickname}")
    public ResponseEntity<Void> deleteRelation(@PathVariable String firstNickname, @PathVariable String secondNickname,Authentication authentication) {
        Relation relation = relationService.getRelationBetweenNicknames(firstNickname, secondNickname);
        if (!Long.valueOf(authentication.getName()).equals(relation.getSender().getId()) &&
        !Long.valueOf(authentication.getName()).equals(relation.getReceiver().getId())) {
            return ResponseEntity.status(403).build();
        }
        relationService.deleteRelation(firstNickname, secondNickname);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/{type}")
    public ResponseEntity<List<RelationResponse>> getRelations(@PathVariable("id") Long id, @PathVariable("type") String type) {
        List<Relation> relations = relationService.getRelationsOf(id, type);
        List<RelationResponse> relationResponses = relations.stream()
                .map(this::toRelationResponse)
                .toList();
        return ResponseEntity.ok(relationResponses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RelationResponse> getRelation(@PathVariable("id") Long id) {
        Relation relation = relationService.getRelation(id);
        return ResponseEntity.ok(toRelationResponse(relation));
    }

    @GetMapping("/between/{firstId}/{secondId}")
    public ResponseEntity<RelationResponse> getRelationBetween(@PathVariable("firstId") Long firstId, @PathVariable("secondId") Long secondId) {
        Relation relation = relationService.getRelationBetween(firstId, secondId);
        return ResponseEntity.ok(toRelationResponse(relation));
    }
    @GetMapping("/between/nicknames/{firstNickname}/{secondNickname}")
    public ResponseEntity<RelationResponse> getRelationBetweenNicknames(@PathVariable("firstNickname") String firstNickname, @PathVariable("secondNickname") String secondNickname) {
        Relation relation = relationService.getRelationBetweenNicknames(firstNickname, secondNickname);
        return ResponseEntity.ok(toRelationResponse(relation));
    }
    @PostMapping("/unblock/{nickname}")
    public ResponseEntity<Void> unblockUser(@PathVariable String nickname, Authentication authentication) {
        relationService.unblock(Long.valueOf(authentication.getName()), nickname);
        return ResponseEntity.ok().build();
    }


    private RelationResponse toRelationResponse(Relation relation) {
        if  (relation == null) return new RelationResponse(
            null,
            null,
            "none",
                false
        );
        return new RelationResponse(
            relation.getSender().getNickname(),
            relation.getReceiver().getNickname(),
            relation.getStatus().toString(),
            relation.isBlocked()
        );
    }
}
