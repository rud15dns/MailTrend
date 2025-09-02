package com.example.mailtrend.oauth.repository;

import com.example.mailtrend.oauth.entity.Category;
import com.example.mailtrend.oauth.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member,Long> {
    boolean existsByEmail(String email);
    Optional<Member> findByEmail(String email);

    @Query("""
           select m.email from Member m
           join m.selectedCategories c
           where c = :category
           """)
    List<String> findEmailsByCategory(Category category);
}
