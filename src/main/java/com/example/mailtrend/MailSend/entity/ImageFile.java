package com.example.mailtrend.MailSend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.catalina.User;

@Getter
@Entity
@NoArgsConstructor
public class ImageFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                    // id

    @Column(nullable = false)           // image 경로
    private String path;



    public ImageFile(String path){
        this.path = path;

    }
}
