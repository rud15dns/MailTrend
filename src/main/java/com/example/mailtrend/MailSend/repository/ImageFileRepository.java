package com.example.mailtrend.MailSend.repository;


import com.example.mailtrend.MailSend.entity.ImageFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageFileRepository extends JpaRepository<ImageFile, Long> {


}
