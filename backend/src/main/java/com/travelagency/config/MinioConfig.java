package com.travelagency.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.SetBucketPolicyArgs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

    private static final Logger log = LoggerFactory.getLogger(MinioConfig.class);

    @Bean
    public MinioClient minioClient(AppProperties props) {
        var m = props.getMinio();
        MinioClient client =
                MinioClient.builder().endpoint(m.getEndpoint()).credentials(m.getAccessKey(), m.getSecretKey()).build();
        try {
            boolean exists =
                    client.bucketExists(BucketExistsArgs.builder().bucket(m.getBucket()).build());
            if (!exists) {
                client.makeBucket(MakeBucketArgs.builder().bucket(m.getBucket()).build());
                log.info("Created MinIO bucket {}", m.getBucket());
            }
            String policy =
                    """
                    {
                      "Version":"2012-10-17",
                      "Statement":[
                        {
                          "Effect":"Allow",
                          "Principal":{"AWS":["*"]},
                          "Action":["s3:GetObject"],
                          "Resource":["arn:aws:s3:::%s/*"]
                        }
                      ]
                    }
                    """
                            .formatted(m.getBucket());
            client.setBucketPolicy(SetBucketPolicyArgs.builder().bucket(m.getBucket()).config(policy).build());
        } catch (Exception e) {
            log.warn("MinIO bucket check failed (is MinIO running?): {}", e.getMessage());
        }
        return client;
    }
}
