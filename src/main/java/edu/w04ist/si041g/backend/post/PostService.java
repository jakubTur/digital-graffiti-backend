package edu.w04ist.si041g.backend.post;

import edu.w04ist.si041g.backend.post.dto.PostRequestBody;
import edu.w04ist.si041g.backend.user.UserRepository;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import edu.w04ist.si041g.backend.post.PostRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@Service
public class PostService {
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private UserRepository userRepository;
    public PostService() {}
    GeometryFactory geom = new GeometryFactory(new PrecisionModel(), 4326);

    public Post createPost(PostRequestBody postRequestBody) {
        Post post = new Post();
        post.setUserId(userRepository.findById(postRequestBody.getUserId()).orElseThrow(() -> new RuntimeException("User not found")));
        post.setLocation(geom.createPoint(new Coordinate(postRequestBody.getLongitude(), postRequestBody.getLatitude())));
        post.setPostedAt(Instant.now());
        return postRepository.save(post);
    }

    public Post getPostById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
    }

    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    public void deletePost(Long id) {
        postRepository.deleteById(id);
    }


    public List<Post> getNearbyPosts(
            double latitude,
            double longitude,
            double radiusMeters
    ) {
        if (!Double.isFinite(latitude)
                || !Double.isFinite(longitude)
                || !Double.isFinite(radiusMeters)
                || latitude < -90 || latitude > 90
                || longitude < -180 || longitude > 180
                || radiusMeters <= 0 || radiusMeters > 20000) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid coordinates or radius"
            );
        }

        return postRepository.findPostsWithinRadius(
                latitude,
                longitude,
                radiusMeters
        );
    }

}
