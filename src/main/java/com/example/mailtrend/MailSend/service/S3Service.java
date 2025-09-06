package com.example.mailtrend.MailSend.service;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Utilities;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${app.s3.prefix:}")
    private String prefix; // 예: "mailtrend/"

    private String key(String fileName) {
        if (prefix == null || prefix.isBlank()) return fileName;
        // prefix가 "mailtrend/" 형태라고 가정
        return prefix + fileName;
    }

    /** 공개 객체라면 정적 URL 반환 */
    public String getThumbnailPath(String fileName) {
        String objectKey = key(fileName);

        // 존재 체크(선택)
        s3.headObject(HeadObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .build());

        // 안전하게 SDK가 생성한 URL 사용
        S3Utilities utils = s3.utilities();
        return utils.getUrl(GetUrlRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .build()).toString();
        // presigned URL이 필요하면 S3Presigner 사용
    }

    public void deleteFile(String fileName) {
        s3.deleteObject(software.amazon.awssdk.services.s3.model.DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key(fileName))
                .build());
    }
}