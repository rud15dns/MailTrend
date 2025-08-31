package com.example.mailtrend.oauth.service;

import com.example.mailtrend.Content.entity.MailContent;
import com.example.mailtrend.Content.repository.MailContentRepository;
import com.example.mailtrend.common.apiPayload.error.CoreException;
import com.example.mailtrend.common.apiPayload.error.GlobalErrorType;
import com.example.mailtrend.oauth.dto.ArchiveResponse;
import com.example.mailtrend.oauth.entity.Category;
import com.example.mailtrend.oauth.entity.Member;
import com.example.mailtrend.oauth.repository.MemberRepository;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.List;
import java.util.Set;

@Service
public class MemberService {
    private final MemberRepository memberRepository;
    private final MailContentRepository mailContentRepository;

    public MemberService(MemberRepository memberRepository, MailContentRepository mailContentRepository) {
        this.memberRepository = memberRepository;
        this.mailContentRepository = mailContentRepository;
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

    public List<ArchiveResponse> getArchive(YearMonth date, List<Category> categories) {
        System.out.println("받은 categories: " + categories); // 디버깅 로그
        System.out.println("categories가 null인가? " + (categories == null));
        System.out.println("categories가 비어있나? " + (categories != null && categories.isEmpty()));


        List<MailContent> list = (categories == null || categories.isEmpty())
                ? mailContentRepository.findAll()
                : mailContentRepository.findByAiSummary_Source_CategoryIn(categories);


        return list.stream().map(ArchiveResponse::from).toList();
    }
}
