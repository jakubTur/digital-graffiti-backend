
package edu.w04ist.si041g.backend.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

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

    public String upload(String type, Long postId, MultipartFile file)
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

        String path = type + "/" + postId + "/"
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

    public String createSignedUrl(String path) {
        if (path == null || path.isBlank()) {
            return null;
        }

        try {
            SignedUrlResult result = client.post()
                    .uri(URI.create(
                            baseUrl + "/storage/v1/object/sign/"
                                    + bucket + "/" + path
                    ))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("expiresIn", 3600))
                    .retrieve()
                    .body(SignedUrlResult.class);

            if (result == null
                    || result.signedURL() == null
                    || result.signedURL().isBlank()) {
                throw new IllegalStateException(
                        "Supabase returned no signed URL for object: " + path
                );
            }

            String url = result.signedURL();

            if (url.startsWith("http://")
                    || url.startsWith("https://")) {
                return url;
            }

            return baseUrl
                    + (url.startsWith("/storage/v1/") ? "" : "/storage/v1")
                    + (url.startsWith("/") ? url : "/" + url);

        } catch (HttpClientErrorException exception) {
            throw new IllegalStateException(
                    "Could not generate signed URL for Supabase object: " + path
                            + ". Supabase response: " + exception.getResponseBodyAsString(),
                    exception
            );
        }
    }

    private record SignedUrlResult(
            @JsonProperty("signedURL") String signedURL
    ) {}

}
