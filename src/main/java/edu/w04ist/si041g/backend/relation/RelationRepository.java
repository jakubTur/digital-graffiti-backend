package edu.w04ist.si041g.backend.relation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RelationRepository extends JpaRepository<Relation, Long> {
    @Query(value = """
        SELECT r
        FROM Relation r
        WHERE (r.sender.id = :userId OR r.receiver.id = :userId)
          AND r.status = :status
        """)
    List<Relation> findRelationsByUserIdAndStatus(@Param("userId") Long userId, @Param("status") RelationStatus status);
    List<Relation> findAllBySender_IdAndStatus(Long senderId, RelationStatus status);
    @Query(value = """
        SELECT r.*
        FROM public.user_relations r
        WHERE (r.inviter = :firstId AND r.invitee = :secondId)
           OR (r.inviter = :secondId AND r.invitee = :firstId)
    """, nativeQuery = true)
    Optional<Relation> findByInviterAndInvitee(@Param("firstId") Long firstId, @Param("secondId") Long secondId);

    @Query(value = """
        SELECT CASE
            WHEN r.inviter = :userId THEN r.invitee
            ELSE r.inviter
        END AS friend_id
        FROM public.user_relations r
        WHERE (r.inviter = :userId OR r.invitee = :userId)
          AND r.status = 'accepted'
    """, nativeQuery = true)
    List<Long> findFriendsOf(@Param("userId") Long userId);
    @Query("""
        SELECT r
        FROM Relation r
        WHERE (r.sender.nickname = :firstNickname AND r.receiver.nickname = :secondNickname)
           OR (r.sender.nickname = :secondNickname AND r.receiver.nickname = :firstNickname)
        """)
    Optional<Relation> findByInviterAndInviteeNicknames(@Param("firstNickname") String firstNickname, @Param("secondNickname") String secondNickname);
}
