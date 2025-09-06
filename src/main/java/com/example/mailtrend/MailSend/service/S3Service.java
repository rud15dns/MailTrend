package com.example.mailtrend.MailSend.service;
import io.micrometer.common.lang.Nullable;
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

    // ---------- 내부 유틸 ----------
    private String normalizedPrefix() {
        if (prefix == null || prefix.isBlank()) return "";
        return prefix.endsWith("/") ? prefix : prefix + "/";
    }

    private String buildKey(String originalFilename) {
        String safeName = (originalFilename == null || originalFilename.isBlank()) ? "file" : originalFilename;
        String datePath = java.time.LocalDate.now().toString().replace("-", "/"); // yyyy/MM/dd
        String uuid = java.util.UUID.randomUUID().toString();
        return normalizedPrefix() + datePath + "/" + uuid + "-" + safeName;
    }

    private String toPublicUrl(String key) {
        return s3.utilities().getUrl(GetUrlRequest.builder().bucket(bucket).key(key).build()).toString();
    }

    // ---------- 조회 ----------
    /** 객체 존재 체크(선택) */
    public boolean exists(String key) {
        try {
            s3.headObject(HeadObjectRequest.builder().bucket(bucket).key(key).build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** 공개 버킷이면 정적 URL 반환 */
    public String getPublicUrl(String key) {
        return toPublicUrl(key);
    }

    // ---------- 업로드 (여러 오버로드) ----------
    /** 바이트 배열 업로드 → 공개 URL 반환 */
    public String upload(byte[] bytes, String filename, @Nullable String contentType) {
        String key = buildKey(filename);
        s3.putObject(
                software.amazon.awssdk.services.s3.model.PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType((contentType == null || contentType.isBlank()) ? "application/octet-stream" : contentType)
                        .build(),
                software.amazon.awssdk.core.sync.RequestBody.fromBytes(bytes)
        );
        return toPublicUrl(key);
    }

    /** InputStream 업로드 → 공개 URL 반환 (대용량 스트림용) */
    public String upload(java.io.InputStream is, long contentLength, String filename, @Nullable String contentType) throws java.io.IOException {
        String key = buildKey(filename);
        s3.putObject(
                software.amazon.awssdk.services.s3.model.PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType((contentType == null || contentType.isBlank()) ? "application/octet-stream" : contentType)
                        .build(),
                software.amazon.awssdk.core.sync.RequestBody.fromInputStream(is, contentLength)
        );
        return toPublicUrl(key);
    }

    /** MultipartFile 업로드 → 공개 URL 반환 (컨트롤러에서 바로 사용) */
    public String upload(org.springframework.web.multipart.MultipartFile file) throws java.io.IOException {
        String key = buildKey(file.getOriginalFilename());
        String ct = (file.getContentType() == null || file.getContentType().isBlank()) ? "application/octet-stream" : file.getContentType();
        s3.putObject(
                software.amazon.awssdk.services.s3.model.PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType(ct)
                        .build(),
                software.amazon.awssdk.core.sync.RequestBody.fromBytes(file.getBytes())
        );
        return toPublicUrl(key);
    }

    /** URL 대신 S3 key만 받고 싶을 때 */
    public String uploadAndReturnKey(byte[] bytes, String filename, @Nullable String contentType) {
        String key = buildKey(filename);
        s3.putObject(
                software.amazon.awssdk.services.s3.model.PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType((contentType == null || contentType.isBlank()) ? "application/octet-stream" : contentType)
                        .build(),
                software.amazon.awssdk.core.sync.RequestBody.fromBytes(bytes)
        );
        return key;
    }

    // ---------- 삭제 ----------
    public void deleteByKey(String key) {
        s3.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
    }

    public void deleteByFilename(String filename) {
        String key = normalizedPrefix() + filename;
        deleteByKey(key);
    }
}