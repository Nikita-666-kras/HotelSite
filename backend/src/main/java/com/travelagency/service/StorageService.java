package com.travelagency.service;

import com.travelagency.config.AppProperties;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import java.io.InputStream;
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
        String publicEndpoint = appProperties.getMinio().getPublicEndpoint();
        if (publicEndpoint == null || publicEndpoint.isBlank()) {
            return null;
        }
        String normalized = publicEndpoint.endsWith("/") ? publicEndpoint.substring(0, publicEndpoint.length() - 1) : publicEndpoint;
        String encodedKey = encodePath(objectKey);
        return normalized + "/" + appProperties.getMinio().getBucket() + "/" + encodedKey;
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

