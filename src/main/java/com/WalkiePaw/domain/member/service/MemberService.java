package com.WalkiePaw.domain.member.service;

import com.WalkiePaw.domain.mail.service.MailService;
import com.WalkiePaw.domain.member.Repository.MemberRepository;
import com.WalkiePaw.domain.member.entity.Member;
import com.WalkiePaw.global.exception.BadRequestException;
import com.WalkiePaw.presentation.domain.member.response.*;
import com.WalkiePaw.presentation.domain.member.request.*;
import com.WalkiePaw.security.CustomPasswordEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.WalkiePaw.global.exception.ExceptionCode.*;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    private final MemberRepository memberRepository;
    private final CustomPasswordEncoder passwordEncoder;
    private final MailService mailService;

    @Transactional(readOnly = true)
    public List<MemberListResponse> findAll() {
        List<Member> memberList = memberRepository.findAll();
        return memberList.stream()
                .map(MemberListResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MemberGetResponse findById(final Long memberId) {
        return MemberGetResponse.from(memberRepository.findById(memberId).orElseThrow(
                () -> new BadRequestException(NOT_FOUND_MEMBER_ID)
        ));
    }

    public Long save(final MemberAddRequest request) {
        Member member = request.toEntity();
        passwordEncoder.encodePassword(member);
        return memberRepository.save(member).getId();
    }

    public void update(final Long id, final MemberUpdateRequest request) {
        Member member = memberRepository.findById(id).orElseThrow(
                () -> new BadRequestException(NOT_FOUND_MEMBER_ID)
        );
        member.updateMember(request);
    }

    @Transactional(readOnly = true)
    public MemberScoreResponse getMemberScore(final Long memberId) {
        return MemberScoreResponse.from(memberRepository.findById(memberId).orElseThrow(
                () -> new BadRequestException(NOT_FOUND_MEMBER_ID)
        ));
    }

    @Transactional(readOnly = true)
    public MemberRRCountResponse getMemberRRCount(final Long memberId) {
        return MemberRRCountResponse.from(memberRepository.findById(memberId).orElseThrow(
                () -> new BadRequestException(NOT_FOUND_MEMBER_ID)
        ));
    }

    public void updatePasswd(final Long memberId, final MemberPasswdUpdateRequest request) {
        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new BadRequestException(NOT_FOUND_MEMBER_ID)
        );
        member.updatePasswd(request.getPassword());
        passwordEncoder.encodePassword(member);
    }

    @Transactional(readOnly = true)
    public Member findByEmail(final String email) {
        return memberRepository.findByEmail(email).orElseThrow(
                () -> new BadRequestException(NOT_FOUND_EMAIL)
        );
    }

    public void draw(final Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new BadRequestException(NOT_FOUND_MEMBER_ID)
        );
        member.withdraw();
    }

    public void ban(final Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new BadRequestException(NOT_FOUND_MEMBER_ID)
        );
        member.ban();
    }

    public void general(final Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new BadRequestException(NOT_FOUND_MEMBER_ID)
        );
        member.general();
    }

    public Page<MemberListResponse> findBySearchCond(final String name, final String nickname, final String email, final Integer reportedCnt, Pageable pageable) {
        return memberRepository.findBySearchCond(name, nickname, email, reportedCnt, pageable);
    }

    public NicknameCheckResponse NicknameCheck(final String nickname) {
        return memberRepository.findByNickname(nickname)
                .map(m -> new NicknameCheckResponse(NCheckResult.DUPLICATED))
                .orElse(new NicknameCheckResponse(NCheckResult.AVAILABLE));
    }


    public FindEmailResponse findEmail(final FindEmailRequest request) {
        Member member = memberRepository.findByNameAndPhoneNumber(request.getName(), request.getPhoneNumber()).orElseThrow(
                () -> new BadRequestException(NOT_FOUND_MEMBER)
        );
        return new FindEmailResponse(maskedMail(member.getEmail()));
    }

    public FindPasswdResponse findPasswd(final FindPasswdRequest request) {
        Optional<Member> member = memberRepository.findByEmailAndName(
            request.getEmail(),
            request.getName());
        if(member.isEmpty()) {
            return new FindPasswdResponse(FindPasswdResult.USER_NOT_FOUND);
        }
        /**
         * TODO
         *  - mail 관련 기능 분리
         */
        Integer authNumber = mailService.makeRandomNumber();
        String setFrom = "no.reply.walkiepaw@gmail.com"; // email-config에 설정한 자신의 이메일 주소를 입력
        String toMail = request.getEmail();
        String title = "인증 이메일 입니다."; // 이메일 제목
        String content =
                "인증 번호는 " + authNumber + "입니다." +
                        "<br>" +
                        "인증번호를 제대로 입력해주세요"; //이메일 내용 삽입
        mailService.mailSend(setFrom, toMail, title, content);
        return new FindPasswdResponse(FindPasswdResult.SUCCESS);
    }

    private String maskedMail(String email) {
        int atIndex = email.indexOf('@');
        String beforeAt = email.substring(0, atIndex);
        String afterAt = email.substring(atIndex);
        String visible = beforeAt.substring(0, beforeAt.length() - 4);
        String masked = "*".repeat(4);
        return visible + masked + afterAt;
    }

    public EmailCheckResponse EmailCheck(final String email) {
        Optional<Member> member = memberRepository.findByEmail(email);
        if (member.isEmpty()) {
            return new EmailCheckResponse(EmailCheckResult.AVAILABLE);
        } else {
            return new EmailCheckResponse(EmailCheckResult.DUPLICATED);
        }
    }

    public ProfileResponse findProfile(final String nickanme) {
        return ProfileResponse.from(memberRepository.findByNickname(nickanme).orElseThrow(
                () -> new BadRequestException(NOT_FOUND_NICKNAME)
                )
        );
    }

    public Long socialSignUp(final SocialSignUpRequest request) {
        Member member = memberRepository.findByEmail(request.getEmail()).orElseThrow(
                () -> new BadRequestException(NOT_FOUND_EMAIL)
        );
        return member.updateBySocialSignUpRequest(request);
    }

    public void updateSeletedAddr(Long memberId, UpdateSelectedAddrRequest request) {
        Member member = memberRepository.findById(memberId).orElseThrow(
            () -> new BadRequestException(NOT_FOUND_MEMBER_ID)
        );
        member.updateSelectedAdrrs(request);
    }

    public AddressesGetResponse getAddressesByMemberId(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(
            () -> new BadRequestException(NOT_FOUND_MEMBER_ID)
        );
        log.info("member: {}", member);
        return AddressesGetResponse.from(member);
    }

    public SideBarInfoResponse getSidebarinfoBy(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new BadRequestException(NOT_FOUND_MEMBER_ID)
        );
        return SideBarInfoResponse.from(member);
    }
}
