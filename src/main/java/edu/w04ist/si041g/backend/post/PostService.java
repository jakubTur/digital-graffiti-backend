package edu.w04ist.si041g.backend.post;

import edu.w04ist.si041g.backend.post.dto.PostRequestBody;
import edu.w04ist.si041g.backend.user.UserRepository;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import edu.w04ist.si041g.backend.post.PostRepository;

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
        post.setTitle(postRequestBody.getTitle());
        post.setText(postRequestBody.getText());
        post.setUserId(userRepository.findById(postRequestBody.getUserId()).orElseThrow(() -> new RuntimeException("User not found")));
        post.setLocation(geom.createPoint(new Coordinate(postRequestBody.getLongitude(), postRequestBody.getLatitude())));
        post.setPostedAt(Instant.now());
        post.setTextColor(postRequestBody.getTextColor());
        return postRepository.save(post);
    }

    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    public void deletePost(Long id) {
        postRepository.deleteById(id);
    }
}
