package com.example.mailtrend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class MailTrendApplication {
    public static void main(String[] args) {
        SpringApplication.run(MailTrendApplication.class, args);
    }
}
