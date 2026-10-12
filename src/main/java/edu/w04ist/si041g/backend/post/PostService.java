package edu.w04ist.si041g.backend.post;

import edu.w04ist.si041g.backend.post.dto.PostRequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import edu.w04ist.si041g.backend.post.PostRepository;

import java.time.Instant;
import java.util.List;

@Service
public class PostService {
    @Autowired
    private PostRepository postRepository;
    public PostService() {}

    public Post createPost(PostRequestBody postRequestBody) {
        Post post = new Post();
        post.setTitle(postRequestBody.getTitle());
        post.setText(postRequestBody.getText());
        post.setUsername(postRequestBody.getUsername());
        post.setLatitude(postRequestBody.getLatitude());
        post.setLongitude(postRequestBody.getLongitude());
        post.setPostedAt(Instant.now());
        post.setTextColor(postRequestBody.getTextColor());
        return postRepository.save(post);
    }

    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }
}
