package com.kltn.scsms_api_service.configs.property;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "aws")
@Data
public class AwsS3Properties {
    
    private String accessKeyId;
    
    private String secretAccessKey;
    
    private String region;
    
    private S3 s3 = new S3();
    
    private MultiPartUpload multiPartUpload = new MultiPartUpload();
    
    @Bean
    public AmazonS3 amazonS3() {
        BasicAWSCredentials credentials = new BasicAWSCredentials(accessKeyId, secretAccessKey);
        
        return AmazonS3ClientBuilder
            .standard()
            .withCredentials(new AWSStaticCredentialsProvider(credentials))
            .withRegion(region)
            .build();
    }
    
    @Data
    public static class S3 {
        private String bucketName;
        
        private Integer imageFileSizeLimit;
        
        private Integer videoFileSizeLimit;
        
        private Integer fileUrlExpiration;
        
        private Integer presignUrlExpiration;
    }
    
    @Data
    public static class MultiPartUpload {
        private Integer partSize;
        
        private Integer threshold;
        
        private Integer maxConcurrency;
        
        private Integer maxAttempts;
        
        private Integer backoffDelay;
    }
}
