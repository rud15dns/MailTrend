package com.example.mailtrend.oauth.repository;

import com.example.mailtrend.oauth.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member,Long> {
    boolean existsByEmail(String email);
}
