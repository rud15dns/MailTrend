package com.example.mailtrend.MailSend.controller;


import com.example.mailtrend.MailSend.service.MailgunService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/mail")
public class MailController {

    private final MailgunService mailgunService;

    public MailController(MailgunService mailgunService) {
        this.mailgunService = mailgunService;
    }

    @PostMapping("/send")
    public Mono<ResponseEntity<String>> send(@RequestBody @Valid SendMailReq req) {
        return mailgunService
                .sendSimpleEmail(req.from(), req.to(), req.subject(), req.text(), req.html())
                .map(ResponseEntity::ok);
    }

    public record SendMailReq(
            @NotBlank String from,
            @NotBlank String to,
            @NotBlank String subject,
            @NotBlank String text,
            String html
    ) {}
}
