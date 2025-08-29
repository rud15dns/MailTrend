package com.example.mailtrend.MailSend.dto;

import lombok.Data;

import java.util.List;

@Data
public class MailMessage {
    private String version;
    private String idempotencyKey;
    private String to;
    private String subject;
    private String summary;
    private String original;
    private Options options;

    @Data public static class Options {
        private String from;
        private List<String> tags;
        private Boolean trackOpens;
    }
}