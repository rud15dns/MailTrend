package com.example.mailtrend.MailSend.controller;

import com.example.mailtrend.MailSend.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class S3Controller {

    private final S3Service s3Service;

    /**
     * 파일 업로드
     * POST /api/images
     * form-data 로 file 업로드
     */
    @PostMapping
    public String upload(@RequestParam("file") MultipartFile file) throws IOException {
        return s3Service.upload(file); // 업로드 후 URL 반환
    }

    /**
     * 특정 파일 URL 조회
     * GET /api/images/{key}
     */
    @GetMapping("/{key}")
    public String getImageUrl(@PathVariable String key) {
        return s3Service.getPublicUrl(key);
    }

    /**
     * 파일 삭제
     * DELETE /api/images/{key}
     */
    @DeleteMapping("/{key}")
    public String delete(@PathVariable String key) {
        s3Service.deleteByKey(key);
        return "success";
    }
}
