package edu.w04ist.si041g.backend.relation;

import edu.w04ist.si041g.backend.relation.dto.RelationRequest;
import edu.w04ist.si041g.backend.relation.dto.RelationResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/relations")
public class RelationController {
    private final RelationService relationService;
    private final JdbcTemplate jdbc;

    public RelationController(RelationService relationService, JdbcTemplate jdbc) {
        this.relationService = relationService;
        this.jdbc = jdbc;
    }

    @PostMapping
    ResponseEntity<Relation> createRelation(@RequestBody RelationRequest relationRequest) {
        Relation relation = relationService.createRelation(relationRequest);
        return ResponseEntity.created(URI.create("/relations/" + relation.getId())).build();
    }
    @GetMapping("/{id}/{type}")
    ResponseEntity<List<RelationResponse>> getRelations(@PathVariable("id") Long id, @PathVariable("type") String type) {
        List<Relation> relations = relationService.getRelationsOf(id, type);
        List<RelationResponse> relationResponses = relations.stream()
                .map(this::toRelationResponse)
                .toList();
        return ResponseEntity.ok(relationResponses);
    }
    @GetMapping("/between/{firstId}/{secondId}")
    ResponseEntity<RelationResponse> getRelationBetween(@PathVariable("firstId") Long firstId, @PathVariable("secondId") Long secondId) {
        Relation relation = relationService.getRelationBetween(firstId, secondId);
        return ResponseEntity.ok(toRelationResponse(relation));
    }
    @GetMapping("/between/nicknames/{firstNickname}/{secondNickname}")
    ResponseEntity<RelationResponse> getRelationBetweenNicknames(@PathVariable("firstNickname") String firstNickname, @PathVariable("secondNickname") String secondNickname) {
        Relation relation = relationService.getRelationBetweenNicknames(firstNickname, secondNickname);
        return ResponseEntity.ok(toRelationResponse(relation));
    }


    private RelationResponse toRelationResponse(Relation relation) {
        if  (relation == null) return new RelationResponse(
            null,
            null,
            "none"
        );
        return new RelationResponse(
            relation.getSender().getNickname(),
            relation.getReceiver().getNickname(),
            relation.getStatus().toString()
        );
    }
}
