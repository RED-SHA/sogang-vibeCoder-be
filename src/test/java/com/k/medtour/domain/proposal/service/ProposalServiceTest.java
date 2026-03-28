package com.k.medtour.domain.proposal.service;

import com.k.medtour.domain.admin.entity.Member;
import com.k.medtour.domain.admin.entity.Role;
import com.k.medtour.domain.admin.repository.MemberRepository;
import com.k.medtour.domain.proposal.dto.ProposalAcceptResponse;
import com.k.medtour.domain.proposal.dto.ProposalCreateRequest;
import com.k.medtour.domain.proposal.dto.ProposalItemDto;
import com.k.medtour.domain.proposal.dto.ProposalListResponse;
import com.k.medtour.domain.proposal.dto.ProposalRejectResponse;
import com.k.medtour.domain.proposal.dto.ProposalRequestCreateRequest;
import com.k.medtour.domain.proposal.dto.ProposalRequestResponse;
import com.k.medtour.domain.proposal.dto.ProposalResponse;
import com.k.medtour.domain.proposal.dto.ProposalSendResponse;
import com.k.medtour.domain.proposal.entity.Proposal;
import com.k.medtour.domain.proposal.entity.ProposalItem;
import com.k.medtour.domain.proposal.entity.ProposalRequest;
import com.k.medtour.domain.proposal.enums.ProposalItemCategory;
import com.k.medtour.domain.proposal.enums.ProposalRequestStatus;
import com.k.medtour.domain.proposal.enums.ProposalStatus;
import com.k.medtour.domain.proposal.repository.ProposalRepository;
import com.k.medtour.domain.proposal.repository.ProposalRequestRepository;
import com.k.medtour.global.common.PageResponse;
import com.k.medtour.global.exception.BusinessException;
import com.k.medtour.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProposalServiceTest {

    @InjectMocks
    private ProposalService proposalService;

    @Mock
    private ProposalRepository proposalRepository;

    @Mock
    private ProposalRequestRepository proposalRequestRepository;

    @Mock
    private MemberRepository memberRepository;

    @Nested
    @DisplayName("견적서 생성")
    class CreateProposal {

        @Test
        @DisplayName("성공 - 견적서 생성 시 금액이 올바르게 계산된다")
        void success_calculateAmounts() {
            // given
            Member patient = createMember(5L);
            given(memberRepository.findById(5L)).willReturn(Optional.of(patient));

            List<ProposalItemDto> items = List.of(
                    new ProposalItemDto(null, ProposalItemCategory.SURGERY, "코 성형",
                            "설명", new BigDecimal("3000.00"), 1, null),
                    new ProposalItemDto(null, ProposalItemCategory.CONCIERGE, "공항 픽업",
                            "설명", new BigDecimal("200.00"), 2, null),
                    new ProposalItemDto(null, ProposalItemCategory.ACCOMMODATION, "호텔 5박",
                            "설명", new BigDecimal("250.00"), 5, null)
            );

            ProposalCreateRequest request = new ProposalCreateRequest(
                    5L, "VIP 패키지", "USD",
                    LocalDateTime.of(2026, 4, 21, 23, 59, 59),
                    items, new BigDecimal("5.0"), "노트"
            );

            given(proposalRepository.save(any(Proposal.class))).willAnswer(invocation -> {
                Proposal saved = invocation.getArgument(0);
                ReflectionTestUtils.setField(saved, "id", 50L);
                return saved;
            });

            // when
            ProposalResponse response = proposalService.createProposal(request);

            // then
            assertThat(response.status()).isEqualTo(ProposalStatus.DRAFT);
            // subtotal: 3000 + 400 + 1250 = 4650
            assertThat(response.subtotal()).isEqualByComparingTo(new BigDecimal("4650.00"));
            // discount: 4650 * 5% = 232.50
            assertThat(response.discountAmount()).isEqualByComparingTo(new BigDecimal("232.50"));
            // total: 4650 - 232.50 = 4417.50
            assertThat(response.totalAmount()).isEqualByComparingTo(new BigDecimal("4417.50"));
            assertThat(response.items()).hasSize(3);
            verify(proposalRepository).save(any(Proposal.class));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 환자")
        void fail_patientNotFound() {
            // given
            given(memberRepository.findById(999L)).willReturn(Optional.empty());
            ProposalCreateRequest request = new ProposalCreateRequest(
                    999L, "제목", "USD", null,
                    List.of(new ProposalItemDto(null, ProposalItemCategory.SURGERY,
                            "수술", null, new BigDecimal("1000"), 1, null)),
                    null, null
            );

            // when & then
            assertThatThrownBy(() -> proposalService.createProposal(request))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.MEMBER_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("견적서 조회")
    class GetProposal {

        @Test
        @DisplayName("성공 - 관리자가 견적서 상세 조회")
        void success_adminAccess() {
            // given
            Proposal proposal = createProposal(50L, 5L);
            given(proposalRepository.findById(50L)).willReturn(Optional.of(proposal));

            // when
            ProposalResponse response = proposalService.getProposal(50L, 1L, "ADMIN");

            // then
            assertThat(response.id()).isEqualTo(50L);
        }

        @Test
        @DisplayName("성공 - 환자 본인 견적서 조회")
        void success_patientOwnAccess() {
            // given
            Proposal proposal = createProposal(50L, 5L);
            given(proposalRepository.findById(50L)).willReturn(Optional.of(proposal));

            // when
            ProposalResponse response = proposalService.getProposal(50L, 5L, "PATIENT");

            // then
            assertThat(response.id()).isEqualTo(50L);
        }

        @Test
        @DisplayName("실패 - 타인의 견적서 접근")
        void fail_accessDenied() {
            // given
            Proposal proposal = createProposal(50L, 5L);
            given(proposalRepository.findById(50L)).willReturn(Optional.of(proposal));

            // when & then
            assertThatThrownBy(() -> proposalService.getProposal(50L, 99L, "PATIENT"))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.PROPOSAL_ACCESS_DENIED);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 견적서")
        void fail_notFound() {
            // given
            given(proposalRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> proposalService.getProposal(999L, 1L, "ADMIN"))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.PROPOSAL_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("견적서 목록 조회")
    class GetProposals {

        @Test
        @DisplayName("성공 - 관리자 목록 조회")
        void success_adminList() {
            // given
            Pageable pageable = PageRequest.of(0, 20);
            Proposal proposal = createProposal(50L, 5L);
            Page<Proposal> page = new PageImpl<>(List.of(proposal), pageable, 1);
            given(proposalRepository.findAllWithFilters(null, null, pageable)).willReturn(page);

            // when
            PageResponse<ProposalListResponse> response = proposalService.getProposals(null, null, pageable);

            // then
            assertThat(response.content()).hasSize(1);
            assertThat(response.totalElements()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("상태 전이")
    class StatusTransition {

        @Test
        @DisplayName("성공 - DRAFT -> SENT")
        void success_send() {
            // given
            Proposal proposal = createProposal(50L, 5L);
            given(proposalRepository.findById(50L)).willReturn(Optional.of(proposal));

            // when
            ProposalSendResponse response = proposalService.sendProposal(50L);

            // then
            assertThat(response.status()).isEqualTo(ProposalStatus.SENT);
            assertThat(response.sentAt()).isNotNull();
        }

        @Test
        @DisplayName("실패 - SENT 상태에서 다시 send 시도")
        void fail_sendFromSent() {
            // given
            Proposal proposal = createProposal(50L, 5L);
            proposal.send(); // DRAFT -> SENT
            given(proposalRepository.findById(50L)).willReturn(Optional.of(proposal));

            // when & then
            assertThatThrownBy(() -> proposalService.sendProposal(50L))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.PROPOSAL_INVALID_STATUS);
        }

        @Test
        @DisplayName("성공 - SENT -> ACCEPTED")
        void success_accept() {
            // given
            Proposal proposal = createProposal(50L, 5L);
            proposal.send();
            given(proposalRepository.findById(50L)).willReturn(Optional.of(proposal));

            // when
            ProposalAcceptResponse response = proposalService.acceptProposal(50L, 5L);

            // then
            assertThat(response.status()).isEqualTo(ProposalStatus.ACCEPTED);
            assertThat(response.respondedAt()).isNotNull();
        }

        @Test
        @DisplayName("성공 - SENT -> REJECTED")
        void success_reject() {
            // given
            Proposal proposal = createProposal(50L, 5L);
            proposal.send();
            given(proposalRepository.findById(50L)).willReturn(Optional.of(proposal));

            // when
            ProposalRejectResponse response = proposalService.rejectProposal(50L, 5L);

            // then
            assertThat(response.status()).isEqualTo(ProposalStatus.REJECTED);
            assertThat(response.respondedAt()).isNotNull();
        }

        @Test
        @DisplayName("실패 - DRAFT 상태에서 accept 시도")
        void fail_acceptFromDraft() {
            // given
            Proposal proposal = createProposal(50L, 5L);
            given(proposalRepository.findById(50L)).willReturn(Optional.of(proposal));

            // when & then
            assertThatThrownBy(() -> proposalService.acceptProposal(50L, 5L))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("실패 - 이미 수락한 견적서 다시 수락 시도")
        void fail_alreadyAccepted() {
            // given
            Proposal proposal = createProposal(50L, 5L);
            proposal.send();
            proposal.accept();
            given(proposalRepository.findById(50L)).willReturn(Optional.of(proposal));

            // when & then
            assertThatThrownBy(() -> proposalService.acceptProposal(50L, 5L))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("견적 요청")
    class CreateProposalRequest {

        @Test
        @DisplayName("성공 - 환자 견적 요청 생성")
        void success() {
            // given
            ProposalRequestCreateRequest request = new ProposalRequestCreateRequest(
                    List.of("코 성형", "피부 레이저"),
                    List.of(1L, 2L),
                    LocalDate.of(2026, 4, 15),
                    LocalDate.of(2026, 4, 25),
                    "5_STAR_HOTEL",
                    List.of("AIRPORT_PICKUP", "INTERPRETER"),
                    new BigDecimal("3000"),
                    new BigDecimal("8000"),
                    "USD",
                    "할랄 식단 필요"
            );

            given(proposalRequestRepository.save(any(ProposalRequest.class)))
                    .willAnswer(invocation -> {
                        ProposalRequest saved = invocation.getArgument(0);
                        ReflectionTestUtils.setField(saved, "id", 30L);
                        return saved;
                    });

            // when
            ProposalRequestResponse response = proposalService.createProposalRequest(5L, request);

            // then
            assertThat(response.requestId()).isEqualTo(30L);
            assertThat(response.status()).isEqualTo(ProposalRequestStatus.PENDING);
            verify(proposalRequestRepository).save(any(ProposalRequest.class));
        }
    }

    @Nested
    @DisplayName("금액 계산")
    class AmountCalculation {

        @Test
        @DisplayName("할인율 0%일 때 subtotal == totalAmount")
        void noDiscount() {
            // given
            Proposal proposal = Proposal.builder()
                    .patientId(1L)
                    .title("테스트")
                    .currency("USD")
                    .discountRate(BigDecimal.ZERO)
                    .build();
            proposal.addItem(ProposalItem.builder()
                    .category(ProposalItemCategory.SURGERY)
                    .name("수술")
                    .unitPrice(new BigDecimal("1000"))
                    .quantity(2)
                    .build());

            // when
            proposal.calculateAmounts();

            // then
            assertThat(proposal.getSubtotal()).isEqualByComparingTo(new BigDecimal("2000"));
            assertThat(proposal.getDiscountAmount()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(proposal.getTotalAmount()).isEqualByComparingTo(new BigDecimal("2000"));
        }

        @Test
        @DisplayName("할인율 10%일 때 정확한 할인 적용")
        void withDiscount() {
            // given
            Proposal proposal = Proposal.builder()
                    .patientId(1L)
                    .title("테스트")
                    .currency("USD")
                    .discountRate(new BigDecimal("10"))
                    .build();
            proposal.addItem(ProposalItem.builder()
                    .category(ProposalItemCategory.SURGERY)
                    .name("수술")
                    .unitPrice(new BigDecimal("1000"))
                    .quantity(1)
                    .build());
            proposal.addItem(ProposalItem.builder()
                    .category(ProposalItemCategory.CONCIERGE)
                    .name("컨시어지")
                    .unitPrice(new BigDecimal("500"))
                    .quantity(2)
                    .build());

            // when
            proposal.calculateAmounts();

            // then
            // subtotal: 1000 + 1000 = 2000
            assertThat(proposal.getSubtotal()).isEqualByComparingTo(new BigDecimal("2000"));
            // discount: 2000 * 10% = 200
            assertThat(proposal.getDiscountAmount()).isEqualByComparingTo(new BigDecimal("200.00"));
            // total: 2000 - 200 = 1800
            assertThat(proposal.getTotalAmount()).isEqualByComparingTo(new BigDecimal("1800.00"));
        }
    }

    // -- Helper methods --

    private Member createMember(Long id) {
        Role role = Role.builder().name("PATIENT").description("환자").build();
        ReflectionTestUtils.setField(role, "id", 3L);
        Member member = Member.builder()
                .email("patient@test.com")
                .name("John Doe")
                .role(role)
                .language("en")
                .build();
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }

    private Proposal createProposal(Long id, Long patientId) {
        Proposal proposal = Proposal.builder()
                .patientId(patientId)
                .title("VIP 패키지")
                .currency("USD")
                .discountRate(new BigDecimal("5"))
                .validUntil(LocalDateTime.of(2026, 4, 21, 23, 59, 59))
                .notes("테스트 노트")
                .build();
        ReflectionTestUtils.setField(proposal, "id", id);
        return proposal;
    }
}
