package com.example.mailtrend.Content.controller;

import com.example.mailtrend.Content.entity.Source;
import com.example.mailtrend.Content.entity.SourceCreateReq;
import com.example.mailtrend.Content.service.SourceService;
import com.example.mailtrend.common.apiPayload.response.ApiResponse;
import com.example.mailtrend.oauth.entity.Category;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/source")
public class SourceCreateController {
    private final SourceService sourceService;

    public SourceCreateController(SourceService sourceService) {
        this.sourceService = sourceService;
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<?>> create(@Valid @RequestBody SourceCreateReq req){
        Category category = req.category();

        Source saved = sourceService.create(req.title(), req.link(), category);

        return ResponseEntity.ok(ApiResponse.success(saved));
    }


}
