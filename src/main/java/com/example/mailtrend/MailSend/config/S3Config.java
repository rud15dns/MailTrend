package com.example.mailtrend.MailSend.config;


import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;


@Configuration
public class S3Config {

    @Value("${spring.cloud.aws.region.static}")
    private String region;

    @Bean
    public S3Client s3Client() {
        // 자격증명은 기본 Provider Chain 사용: 환경변수/EC2 role 등
        return S3Client.builder()
                .region(Region.of(region))
                .build();
    }
}