package com.k.medtour.domain.proposal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.k.medtour.domain.proposal.dto.ProposalAcceptResponse;
import com.k.medtour.domain.proposal.dto.ProposalCreateRequest;
import com.k.medtour.domain.proposal.dto.ProposalItemDto;
import com.k.medtour.domain.proposal.dto.ProposalListResponse;
import com.k.medtour.domain.proposal.dto.ProposalRejectResponse;
import com.k.medtour.domain.proposal.dto.ProposalRequestCreateRequest;
import com.k.medtour.domain.proposal.dto.ProposalRequestResponse;
import com.k.medtour.domain.proposal.dto.ProposalResponse;
import com.k.medtour.domain.proposal.dto.ProposalSendResponse;
import com.k.medtour.domain.proposal.enums.ProposalItemCategory;
import com.k.medtour.domain.proposal.enums.ProposalRequestStatus;
import com.k.medtour.domain.proposal.enums.ProposalStatus;
import com.k.medtour.domain.proposal.service.ProposalService;
import com.k.medtour.global.auth.jwt.JwtTokenProvider;
import com.k.medtour.global.common.PageResponse;
import com.k.medtour.global.exception.BusinessException;
import com.k.medtour.global.exception.ErrorCode;
import com.k.medtour.support.SecurityTestUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProposalController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProposalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProposalService proposalService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @AfterEach
    void tearDown() {
        SecurityTestUtil.clearAuthentication();
    }

    @Nested
    @DisplayName("POST /api/v1/proposals - 견적서 생성")
    class CreateProposal {

        @BeforeEach
        void setUp() {
            SecurityTestUtil.setAuthentication(1L, "ADMIN");
        }

        @Test
        @DisplayName("성공 - 201 Created")
        void success() throws Exception {
            // given
            List<ProposalItemDto> items = List.of(
                    new ProposalItemDto(1L, ProposalItemCategory.SURGERY, "코 성형",
                            "설명", new BigDecimal("3000.00"), 1, new BigDecimal("3000.00"))
            );
            ProposalResponse response = new ProposalResponse(
                    50L, 5L, "VIP 패키지", ProposalStatus.DRAFT, "USD",
                    items, new BigDecimal("3000.00"), new BigDecimal("5.0"),
                    new BigDecimal("150.00"), new BigDecimal("2850.00"),
                    LocalDateTime.of(2026, 4, 21, 23, 59, 59),
                    "노트", LocalDateTime.now(), null, null
            );
            given(proposalService.createProposal(any(ProposalCreateRequest.class))).willReturn(response);

            ProposalCreateRequest request = new ProposalCreateRequest(
                    5L, "VIP 패키지", "USD",
                    LocalDateTime.of(2026, 4, 21, 23, 59, 59),
                    List.of(new ProposalItemDto(null, ProposalItemCategory.SURGERY,
                            "코 성형", "설명", new BigDecimal("3000"), 1, null)),
                    new BigDecimal("5.0"), "노트"
            );

            // when & then
            mockMvc.perform(post("/api/v1/proposals")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(50))
                    .andExpect(jsonPath("$.data.status").value("DRAFT"));
        }

        @Test
        @DisplayName("실패 - 필수 항목 누락 400")
        void fail_validation() throws Exception {
            ProposalCreateRequest request = new ProposalCreateRequest(
                    null, "", "USD", null, List.of(), null, null
            );

            mockMvc.perform(post("/api/v1/proposals")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/proposals - 견적서 목록 조회")
    class GetProposals {

        @BeforeEach
        void setUp() {
            SecurityTestUtil.setAuthentication(1L, "ADMIN");
        }

        @Test
        @DisplayName("성공 - 200 OK")
        void success() throws Exception {
            List<ProposalListResponse> content = List.of(
                    new ProposalListResponse(50L, 5L, "VIP 패키지",
                            ProposalStatus.SENT, new BigDecimal("4227.50"), "USD",
                            LocalDateTime.of(2026, 4, 21, 23, 59, 59),
                            LocalDateTime.now())
            );
            PageResponse<ProposalListResponse> pageResponse =
                    new PageResponse<>(content, 0, 20, 1, 1);
            given(proposalService.getProposals(any(), any(), any())).willReturn(pageResponse);

            mockMvc.perform(get("/api/v1/proposals"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content[0].id").value(50));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/proposals/{id}/send - 견적서 발송")
    class SendProposal {

        @BeforeEach
        void setUp() {
            SecurityTestUtil.setAuthentication(1L, "ADMIN");
        }

        @Test
        @DisplayName("성공 - 200 OK")
        void success() throws Exception {
            ProposalSendResponse response = new ProposalSendResponse(
                    50L, ProposalStatus.SENT, LocalDateTime.now());
            given(proposalService.sendProposal(50L)).willReturn(response);

            mockMvc.perform(post("/api/v1/proposals/50/send"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value("SENT"));
        }

        @Test
        @DisplayName("실패 - 잘못된 상태 400")
        void fail_invalidStatus() throws Exception {
            given(proposalService.sendProposal(50L))
                    .willThrow(new BusinessException(ErrorCode.PROPOSAL_INVALID_STATUS));

            mockMvc.perform(post("/api/v1/proposals/50/send"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/proposals/request - 견적 요청")
    class CreateProposalRequestTest {

        @BeforeEach
        void setUp() {
            SecurityTestUtil.setAuthentication(5L, "PATIENT");
        }

        @Test
        @DisplayName("성공 - 201 Created")
        void success() throws Exception {
            ProposalRequestResponse response = new ProposalRequestResponse(
                    30L, ProposalRequestStatus.PENDING, LocalDateTime.now());
            given(proposalService.createProposalRequest(any(), any(ProposalRequestCreateRequest.class)))
                    .willReturn(response);

            ProposalRequestCreateRequest request = new ProposalRequestCreateRequest(
                    List.of("코 성형"), List.of(1L),
                    LocalDate.of(2026, 4, 15), LocalDate.of(2026, 4, 25),
                    "5_STAR_HOTEL", List.of("AIRPORT_PICKUP"),
                    new BigDecimal("3000"), new BigDecimal("8000"),
                    "USD", "할랄 식단"
            );

            mockMvc.perform(post("/api/v1/proposals/request")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.requestId").value(30))
                    .andExpect(jsonPath("$.data.status").value("PENDING"));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/proposals/{id}/accept - 견적서 수락")
    class AcceptProposal {

        @BeforeEach
        void setUp() {
            SecurityTestUtil.setAuthentication(5L, "PATIENT");
        }

        @Test
        @DisplayName("성공 - 200 OK")
        void success() throws Exception {
            ProposalAcceptResponse response = new ProposalAcceptResponse(
                    50L, ProposalStatus.ACCEPTED, LocalDateTime.now());
            given(proposalService.acceptProposal(eq(50L), any())).willReturn(response);

            mockMvc.perform(post("/api/v1/proposals/50/accept"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value("ACCEPTED"));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/proposals/{id}/reject - 견적서 거절")
    class RejectProposal {

        @BeforeEach
        void setUp() {
            SecurityTestUtil.setAuthentication(5L, "PATIENT");
        }

        @Test
        @DisplayName("성공 - 200 OK")
        void success() throws Exception {
            ProposalRejectResponse response = new ProposalRejectResponse(
                    50L, ProposalStatus.REJECTED, LocalDateTime.now());
            given(proposalService.rejectProposal(eq(50L), any())).willReturn(response);

            mockMvc.perform(post("/api/v1/proposals/50/reject"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value("REJECTED"));
        }

        @Test
        @DisplayName("실패 - 이미 응답한 견적서 400")
        void fail_alreadyResponded() throws Exception {
            given(proposalService.rejectProposal(eq(50L), any()))
                    .willThrow(new BusinessException(ErrorCode.PROPOSAL_ALREADY_RESPONDED));

            mockMvc.perform(post("/api/v1/proposals/50/reject"))
                    .andExpect(status().isBadRequest());
        }
    }
}
