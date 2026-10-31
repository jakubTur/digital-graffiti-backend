
package edu.w04ist.si041g.backend.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.UUID;

@Service
public class SupabaseStorageService {

    private final RestClient client;
    private final String baseUrl;
    private final String bucket;

    public SupabaseStorageService(
            @Value("${supabase.url}") String baseUrl,
            @Value("${supabase.secret-key}") String secretKey,
            @Value("${supabase.storage.bucket}") String bucket
    ) {
        this.baseUrl = baseUrl.replaceAll("/+$", "");
        this.bucket = bucket;

        this.client = RestClient.builder()
                .defaultHeader("apikey", secretKey)
                .build();
    }

    public String upload(Long postId, MultipartFile file)
            throws IOException {

        if (file.isEmpty() || file.getSize() > 5 * 1024 * 1024) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid image size"
            );
        }

        String contentType = file.getContentType();

        String extension = switch (contentType == null ? "" : contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only JPEG and PNG images are supported"
            );
        };

        byte[] bytes = file.getBytes();

        if (ImageIO.read(new ByteArrayInputStream(bytes)) == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid image"
            );
        }

        String path = "posts/" + postId + "/"
                + UUID.randomUUID() + extension;

        client.post()
                .uri(objectUri(path))
                .contentType(MediaType.parseMediaType(contentType))
                .body(bytes)
                .retrieve()
                .toBodilessEntity();

        return path;
    }

    public void delete(String path) {
        client.delete()
                .uri(objectUri(path))
                .retrieve()
                .toBodilessEntity();
    }

    private URI objectUri(String path) {
        return URI.create(
                baseUrl + "/storage/v1/object/"
                        + bucket + "/" + path
        );
    }
}
