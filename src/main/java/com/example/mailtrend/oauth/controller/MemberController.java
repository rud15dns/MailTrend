package com.example.mailtrend.oauth.controller;

import com.example.mailtrend.common.apiPayload.error.CoreException;
import com.example.mailtrend.common.apiPayload.error.GlobalErrorType;
import com.example.mailtrend.common.apiPayload.response.ApiResponse;
import com.example.mailtrend.oauth.dto.ArchiveRequest;
import com.example.mailtrend.oauth.dto.MemberRequest;
import com.example.mailtrend.oauth.service.MemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class MemberController {
    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping("/subscribe")
    public ResponseEntity<ApiResponse<?>> subscribe(@RequestBody MemberRequest request){
        if (request.getEmail() == null)
            throw new CoreException(GlobalErrorType.INVALID_EMAIL);

        if (request.getCategories() == null || request.getCategories().isEmpty())
            throw new CoreException(GlobalErrorType.INVALID_CATEGORIES);

        memberService.subscribe(request.getEmail(), request.getCategories());
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping("/unsubscribe")
    public ResponseEntity<ApiResponse<?>> unsubscribe(@RequestBody MemberRequest request){
        if (request.getEmail() == null)
            throw new CoreException(GlobalErrorType.INVALID_EMAIL);

        memberService.unsubscribe(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PutMapping("/change-category")
    public ResponseEntity<ApiResponse<?>> changeCategory(@RequestBody MemberRequest request){
        if (request.getEmail() == null)
            throw new CoreException(GlobalErrorType.INVALID_EMAIL);

        memberService.changeCategory(request.getEmail(), request.getCategories());
        return ResponseEntity.ok(ApiResponse.success());
    }

    @GetMapping("/archive")
    public ResponseEntity<ApiResponse<?>> archive(ArchiveRequest request){
        // request.getDate() -> 2025-08
        // request.getCategory() -> [Category.AI, Category.BACKEND]

        return ResponseEntity.ok(ApiResponse.success());
    }
}
