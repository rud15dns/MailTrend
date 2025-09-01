package com.example.mailtrend.MailSend.entity;

import lombok.experimental.UtilityClass;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@UtilityClass
public class IdempoUtil {
    public String sha256(String to, String subject, String original) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String seed = (to + "\n" + subject + "\n" + original).trim();
            byte[] digest = md.digest(seed.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
