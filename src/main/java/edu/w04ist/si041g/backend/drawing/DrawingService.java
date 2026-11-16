package edu.w04ist.si041g.backend.drawing;

import edu.w04ist.si041g.backend.drawing.dto.DrawingRequestBody;
import edu.w04ist.si041g.backend.post.PostRepository;
import edu.w04ist.si041g.backend.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class DrawingService {
    @Autowired
    private DrawingRepository drawingRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PostRepository postRepository;
    public DrawingService(){}

    public Drawing addDrawing(DrawingRequestBody drawing){
        Drawing newDrawing = new Drawing();
        newDrawing.setUserId(userRepository.findById(drawing.getUserId()).orElseThrow(() -> new RuntimeException("User not found")));
        newDrawing.setPost(postRepository.findById(drawing.getPostId()).orElseThrow(() -> new RuntimeException("Post not found")));
        if (drawing.getOriginId() != null) {
            newDrawing.setOrigin(drawingRepository.findById(drawing.getOriginId()).orElseThrow(() -> new RuntimeException("Origin drawing not found")));
        }
        newDrawing.setCreatedAt(Instant.now());
        newDrawing.setActive(true);
        return drawingRepository.save(newDrawing);
    }
    public List<Drawing> getDrawingByPostId(Long postId){
        return drawingRepository.findByPostId(postId);
    }
    public Drawing getDrawingById(Long id){
        return drawingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Drawing not found"));
    }
    public void deleteDrawing(Long id){
        Drawing drawing = getDrawingById(id);
        if (drawing == null) {
            return;
        }
        drawing.setActive(false);
        drawingRepository.save(drawing);
    }
}
