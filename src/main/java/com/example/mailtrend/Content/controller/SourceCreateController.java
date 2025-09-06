package com.example.mailtrend.Content.controller;

import com.example.mailtrend.Content.entity.Source;
import com.example.mailtrend.Content.entity.SourceCreateReq;
import com.example.mailtrend.Content.service.SourceService;
import com.example.mailtrend.common.apiPayload.response.ApiResponse;
import com.example.mailtrend.oauth.entity.Category;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.example.mailtrend.common.apiPayload.error.GlobalErrorType.SOURCE_UNCREATED;

@RestController
@RequestMapping("/source")
@RequiredArgsConstructor
public class SourceCreateController {
    private final SourceService sourceService;



    @PostMapping("/createbatch")
    public ResponseEntity<ApiResponse<List<Source>>> createBatch(
            @Valid @RequestBody List<SourceCreateReq> reqs) {
        if (reqs.size() != 5) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(SOURCE_UNCREATED));
        }

    


        List<Source> saved = sourceService.createBatch(reqs);
        return ResponseEntity.ok(ApiResponse.success(saved));
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<?>> create(
            @Valid @RequestBody SourceCreateReq reqs) {
        String description = reqs.getDescription();
        Category category = reqs.getCategory();
        String link = reqs.getLink();
        String title = reqs.getTitle();
        Source saved = sourceService.create(title,link,description,category);
        return ResponseEntity.ok(ApiResponse.success(saved));
    }



}
