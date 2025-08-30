package com.example.mailtrend.oauth.service;

import com.example.mailtrend.common.apiPayload.error.CoreException;
import com.example.mailtrend.common.apiPayload.error.GlobalErrorType;
import com.example.mailtrend.oauth.entity.Category;
import com.example.mailtrend.oauth.entity.Member;
import com.example.mailtrend.oauth.repository.MemberRepository;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class MemberService {
    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public void subscribe(String email, Set<Category> categories) {
        if (memberRepository.existsByEmail(email)){
            throw new CoreException(GlobalErrorType.DUPLICATE_EMAIL);
        }

        Member member = new Member(email, categories);
        memberRepository.save(member);
    }

    public void unsubscribe(String email){
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CoreException(GlobalErrorType.MEMBER_NOT_FOUND));

        memberRepository.delete(member);
    }

    public void changeCategory(String email, Set<Category> categories) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CoreException(GlobalErrorType.MEMBER_NOT_FOUND));

        member.updateSelectedCategories(categories);
        memberRepository.save(member);
    }
}
