package com.example.mailtrend.MailSend.controller;

import com.example.mailtrend.MailSend.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class S3Controller {
    private final S3Service s3Uploader;

    @GetMapping("/images")
    public String getImage(@AuthenticationPrincipal UserDetailsImpl userDetails)throws IOException {
        return s3Uploader.getThumbnailPath("img.png");
    }

    @PostMapping("/images")
    public String delete(@RequestParam(required = true) String fileName){
        s3Uploader.deleteFile(fileName);
        return "success";
    }
}
