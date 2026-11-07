package edu.w04ist.si041g.backend.post;

import edu.w04ist.si041g.backend.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post,Long> {

    void deleteAllByUserId_Id(Long userId);


    @Query(value = """
        SELECT p.*
        FROM public.posts_new_tmp p
        WHERE extensions.ST_DWithin(
            p.location,
            CAST(
                extensions.ST_SetSRID(
                    extensions.ST_MakePoint(:longitude, :latitude),
                    4326
                ) AS extensions.geography
            ),
            :radiusMeters
        )
        ORDER BY extensions.ST_Distance(
            p.location,
            CAST(
                extensions.ST_SetSRID(
                    extensions.ST_MakePoint(:longitude, :latitude),
                    4326
                ) AS extensions.geography
            )
        )
        """, nativeQuery = true)
    List<Post> findPostsWithinRadius(
            @Param("latitude") double latitude,
            @Param("longitude") double longitude,
            @Param("radiusMeters") double radiusMeters
    );

}
