package com.example.mailtrend.oauth.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    @Enumerated(EnumType.STRING)
    @ElementCollection
    @CollectionTable(name="member_categories")
    private Set<Category> selectedCategories = new HashSet<>();

    public Member(String email, Set<Category> selectedCategories){
        this.email = email;
        this.selectedCategories = selectedCategories;
    }

    public void updateSelectedCategories(Set<Category> selectedCategories) {
        this.selectedCategories = selectedCategories;
    }
}
