package com.travelagency.service;

import com.travelagency.config.AppProperties;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import java.io.InputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class StorageService {

    private final MinioClient minioClient;
    private final MinioClient minioPublicClient;
    private final AppProperties appProperties;

    public StorageService(MinioClient minioClient, AppProperties appProperties) {
        this.minioClient = minioClient;
        this.appProperties = appProperties;
        String publicEndpoint = appProperties.getMinio().getPublicEndpoint();
        if (publicEndpoint != null && !publicEndpoint.isBlank()) {
            this.minioPublicClient =
                    MinioClient.builder()
                            .endpoint(publicEndpoint)
                            .credentials(appProperties.getMinio().getAccessKey(), appProperties.getMinio().getSecretKey())
                            .build();
        } else {
            this.minioPublicClient = minioClient;
        }
    }

    /**
     * Для API: ключ объекта в MinIO или устаревший полный URL (localhost / travel-media / …) —
     * пересобирается в относительный URL {@code /api/media/...}, который отдаёт backend из MinIO.
     */
    public String resolveDisplayUrl(String stored) {
        if (stored == null || stored.isBlank()) {
            return null;
        }
        String s = stored.trim();
        if (!s.startsWith("http://") && !s.startsWith("https://")) {
            return presignedGetUrl(s);
        }
        String objectKey = tryExtractBucketObjectKey(s);
        if (objectKey != null && !objectKey.isBlank()) {
            return presignedGetUrl(objectKey);
        }
        return s;
    }

    public String presignedGetUrl(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            return null;
        }
        String publicUrl = publicObjectUrl(objectKey);
        if (publicUrl != null) {
            return publicUrl;
        }
        try {
            return minioPublicClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(appProperties.getMinio().getBucket())
                            .object(objectKey)
                            .expiry(1, TimeUnit.HOURS)
                            .build());
        } catch (Exception e) {
            return null;
        }
    }

    private String publicObjectUrl(String objectKey) {
        String encodedKey = encodePath(objectKey);
        return "/api/media/" + encodedKey;
    }

    /** Извлекает object key из path-style URL .../bucketName/key или .../api/media/key. */
    private String tryExtractBucketObjectKey(String url) {
        try {
            URI uri = URI.create(url.trim());
            String path = uri.getPath();
            if (path == null || path.isEmpty()) {
                return null;
            }
            String apiPrefix = "/api/media/";
            if (path.startsWith(apiPrefix)) {
                String tail = path.substring(apiPrefix.length());
                if (tail.isEmpty()) {
                    return null;
                }
                return decodeObjectKeyPath(tail);
            }
            String bucket = appProperties.getMinio().getBucket();
            String prefix = "/" + bucket + "/";
            if (!path.startsWith(prefix)) {
                return null;
            }
            String encodedTail = path.substring(prefix.length());
            return decodeObjectKeyPath(encodedTail);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static String decodeObjectKeyPath(String pathEncoded) {
        if (pathEncoded == null || pathEncoded.isEmpty()) {
            return "";
        }
        String[] seg = pathEncoded.split("/");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < seg.length; i++) {
            if (i > 0) {
                sb.append('/');
            }
            sb.append(URLDecoder.decode(seg[i], StandardCharsets.UTF_8).replace('+', ' '));
        }
        return sb.toString();
    }

    private static String encodePath(String objectKey) {
        String[] parts = objectKey.split("/");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) sb.append('/');
            sb.append(URLEncoder.encode(parts[i], StandardCharsets.UTF_8).replace("+", "%20"));
        }
        return sb.toString();
    }

    /** Uploads file to MinIO; returns object key (not URL). */
    public String upload(String prefix, MultipartFile file) {
        String orig = file.getOriginalFilename();
        String ext = extension(orig);
        String key = prefix + "/" + UUID.randomUUID() + (ext.isEmpty() ? "" : "." + ext);
        try (InputStream in = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(appProperties.getMinio().getBucket())
                            .object(key)
                            .stream(in, file.getSize(), -1)
                            .contentType(file.getContentType() != null ? file.getContentType() : "application/octet-stream")
                            .build());
        } catch (Exception e) {
            throw new BadRequestException("Не удалось загрузить файл: " + e.getMessage());
        }
        return key;
    }

    public void deleteObject(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            return;
        }
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(appProperties.getMinio().getBucket())
                            .object(objectKey)
                            .build());
        } catch (Exception ignored) {
            // ignore missing object
        }
    }

    private static String extension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        String n = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        return n.length() > 8 ? "" : n;
    }
}

