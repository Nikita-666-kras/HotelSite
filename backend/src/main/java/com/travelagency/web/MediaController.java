package com.travelagency.web;

import com.travelagency.config.AppProperties;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import java.io.IOException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
public class MediaController {

    private final MinioClient minioClient;
    private final AppProperties appProperties;

    public MediaController(MinioClient minioClient, AppProperties appProperties) {
        this.minioClient = minioClient;
        this.appProperties = appProperties;
    }

    /**
     * Public object bytes (same origin as SPA). Avoids exposing MinIO on the public host and
     * works through any reverse proxy that already routes {@code /api/} to the backend.
     */
    @GetMapping("/api/media/{*objectKey}")
    public ResponseEntity<StreamingResponseBody> get(@PathVariable("objectKey") String objectKey) {
        if (objectKey == null || objectKey.isBlank() || objectKey.contains("..")) {
            return ResponseEntity.badRequest().build();
        }
        String bucket = appProperties.getMinio().getBucket();
        try {
            StatObjectResponse stat =
                    minioClient.statObject(
                            StatObjectArgs.builder().bucket(bucket).object(objectKey).build());
            String contentType = stat.contentType();
            if (contentType == null || contentType.isBlank()) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }
            long size = stat.size();
            MediaType mediaType = MediaType.parseMediaType(contentType);

            StreamingResponseBody body =
                    outputStream -> {
                        try (GetObjectResponse in =
                                minioClient.getObject(
                                        GetObjectArgs.builder().bucket(bucket).object(objectKey).build())) {
                            in.transferTo(outputStream);
                        } catch (Exception e) {
                            throw new IOException(e.getMessage(), e);
                        }
                    };

            ResponseEntity.BodyBuilder builder =
                    ResponseEntity.ok()
                            .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
                            .contentType(mediaType);
            if (size >= 0) {
                builder.contentLength(size);
            }
            return builder.body(body);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
