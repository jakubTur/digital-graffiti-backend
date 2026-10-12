package edu.w04ist.si041g.backend.post;

import edu.w04ist.si041g.backend.post.dto.PostRequestBody;
import edu.w04ist.si041g.backend.post.dto.PostResponseBody;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/posts")
public class PostController {
    private final PostService postService;
    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping("/create")
    public ResponseEntity<Post> createPost(@RequestBody PostRequestBody post) {
        Post createdPost = postService.createPost(post);
        return ResponseEntity.created(URI.create("/posts/" + createdPost.getId())).build();
    }

    @GetMapping("/all")
    public ResponseEntity<List<PostResponseBody>> getAllPosts() {
        return ResponseEntity.ok(postService.getAllPosts().stream().map(this::toPostResponseBody).toList());
    }

    private PostResponseBody toPostResponseBody(Post post) {
        return new PostResponseBody(
                post.getId(),
                post.getTitle(),
                post.getText(),
                post.getPostedAt(),
                post.getTextColor(),
                post.getUsername(),
                post.getLatitude(),
                post.getLongitude()
        );
    }
}
