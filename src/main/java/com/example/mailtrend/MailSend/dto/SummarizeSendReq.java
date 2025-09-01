package com.example.mailtrend.MailSend.dto;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record SummarizeSendReq(
        @NotBlank String to,
        @NotBlank String subject,
        @NotBlank String original,
        @Min(20) @Max(400) int maxWords
) {}