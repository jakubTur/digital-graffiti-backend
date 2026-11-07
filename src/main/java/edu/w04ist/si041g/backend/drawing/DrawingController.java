package edu.w04ist.si041g.backend.drawing;

import edu.w04ist.si041g.backend.drawing.dto.DrawingRequestBody;
import edu.w04ist.si041g.backend.drawing.dto.DrawingResponseBody;
import edu.w04ist.si041g.backend.storage.SupabaseStorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts/drawings")
public class DrawingController {
    private final DrawingService drawingService;
    private final SupabaseStorageService storage;

    public DrawingController(DrawingService drawingService, SupabaseStorageService storage) {
        this.storage = storage;
        this.drawingService = drawingService;
    }

    @PostMapping("/add")
    public ResponseEntity<DrawingResponseBody> addDrawing(@RequestBody DrawingRequestBody drawingRequestBody) {
        Drawing createdDrawing = drawingService.addDrawing(drawingRequestBody);
        return ResponseEntity.ok(toDrawingResponseBody(createdDrawing));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<List<DrawingResponseBody>> getDrawingByPostId(@PathVariable Long postId) {
        return ResponseEntity.ok(drawingService.getDrawingByPostId(postId).stream().map(this::toDrawingResponseBody).toList());
    }

    private DrawingResponseBody toDrawingResponseBody(Drawing drawing) {
        String imageUrl = drawing.getImagePath() == null
                ? null
                : storage.createSignedUrl(drawing.getImagePath());
        return new DrawingResponseBody(
                drawing.getId(),
                drawing.getUserId().getNickname(),
                drawing.getPost().getId(),
                drawing.getOrigin() == null
                        ? null
                        : drawing.getOrigin().getId(),
                imageUrl
        );
    }
}
