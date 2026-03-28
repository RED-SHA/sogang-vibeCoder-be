package com.k.medtour.domain.aftercare.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.k.medtour.domain.aftercare.dto.AftercareGuideCreateRequest;
import com.k.medtour.domain.aftercare.dto.AftercareGuideResponse;
import com.k.medtour.domain.aftercare.dto.InvoiceCreateRequest;
import com.k.medtour.domain.aftercare.dto.InvoiceResponse;
import com.k.medtour.domain.aftercare.dto.StaffReportCreateRequest;
import com.k.medtour.domain.aftercare.dto.StaffReportResponse;
import com.k.medtour.domain.aftercare.enums.InvoiceStatus;
import com.k.medtour.domain.aftercare.service.AftercareService;
import com.k.medtour.global.auth.jwt.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AftercareController.class)
@AutoConfigureMockMvc(addFilters = false)
class AftercareControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AftercareService aftercareService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Nested
    @DisplayName("POST /api/v1/aftercare/guides")
    class CreateGuideTest {

        @Test
        @DisplayName("성공 - 가이드를 생성한다")
        @WithMockUser(roles = "ADMIN")
        void createGuide_success() throws Exception {
            AftercareGuideCreateRequest request = new AftercareGuideCreateRequest(
                    1L, "Post-Surgery Guide", "Rest well",
                    Map.of("day1", "rest")
            );
            AftercareGuideResponse response = new AftercareGuideResponse(
                    1L, 1L, "Post-Surgery Guide", "Rest well",
                    Map.of("day1", "rest"), LocalDateTime.now(), LocalDateTime.now()
            );
            given(aftercareService.createGuide(any())).willReturn(response);

            mockMvc.perform(post("/api/v1/aftercare/guides")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.title").value("Post-Surgery Guide"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/aftercare/guides/{journeyId}")
    class GetGuideTest {

        @Test
        @DisplayName("성공 - 가이드를 조회한다")
        @WithMockUser(roles = "PATIENT")
        void getGuide_success() throws Exception {
            AftercareGuideResponse response = new AftercareGuideResponse(
                    1L, 1L, "Guide", "Content", null, LocalDateTime.now(), LocalDateTime.now()
            );
            given(aftercareService.getGuide(1L)).willReturn(response);

            mockMvc.perform(get("/api/v1/aftercare/guides/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.title").value("Guide"));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/aftercare/invoices")
    class CreateInvoiceTest {

        @Test
        @DisplayName("성공 - 인보이스를 생성한다")
        @WithMockUser(roles = "ADMIN")
        void createInvoice_success() throws Exception {
            InvoiceCreateRequest request = new InvoiceCreateRequest(
                    1L, 2L, "USD", LocalDate.now().plusDays(30),
                    List.of(new InvoiceCreateRequest.InvoiceItemRequest("Surgery", new BigDecimal("5000"), 1))
            );
            InvoiceResponse response = new InvoiceResponse(
                    1L, 1L, 2L, "INV-20260328-0001", "USD",
                    new BigDecimal("5000"), new BigDecimal("500"), new BigDecimal("5500"),
                    InvoiceStatus.DRAFT, LocalDateTime.now(), LocalDate.now().plusDays(30),
                    List.of(new InvoiceResponse.InvoiceItemResponse(
                            1L, "Surgery", new BigDecimal("5000"), 1, new BigDecimal("5000"))),
                    LocalDateTime.now()
            );
            given(aftercareService.createInvoice(any())).willReturn(response);

            mockMvc.perform(post("/api/v1/aftercare/invoices")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.invoiceNumber").value("INV-20260328-0001"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/aftercare/invoices/{journeyId}")
    class GetInvoiceTest {

        @Test
        @DisplayName("성공 - 인보이스를 조회한다")
        @WithMockUser(roles = "ADMIN")
        void getInvoice_success() throws Exception {
            InvoiceResponse response = new InvoiceResponse(
                    1L, 1L, 2L, "INV-20260328-0001", "USD",
                    new BigDecimal("5000"), new BigDecimal("500"), new BigDecimal("5500"),
                    InvoiceStatus.DRAFT, LocalDateTime.now(), LocalDate.now().plusDays(30),
                    List.of(), LocalDateTime.now()
            );
            given(aftercareService.getInvoiceByJourneyId(1L)).willReturn(response);

            mockMvc.perform(get("/api/v1/aftercare/invoices/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.currency").value("USD"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/aftercare/invoices/me")
    class GetMyInvoicesTest {

        @Test
        @DisplayName("성공 - 내 인보이스를 반환한다")
        @WithMockUser(roles = "PATIENT")
        void getMyInvoices_success() throws Exception {
            given(aftercareService.getMyInvoices(anyLong())).willReturn(List.of());

            mockMvc.perform(get("/api/v1/aftercare/invoices/me"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/aftercare/reports")
    class CreateReportTest {

        @Test
        @DisplayName("성공 - 리포트를 생성한다")
        @WithMockUser(roles = "STAFF")
        void createReport_success() throws Exception {
            StaffReportCreateRequest request = new StaffReportCreateRequest(
                    1L, "Completed all tasks", 8.0);
            StaffReportResponse response = new StaffReportResponse(
                    1L, 1L, 5L, "Completed all tasks", 8.0,
                    LocalDateTime.now(), LocalDateTime.now()
            );
            given(aftercareService.createReport(any(), anyLong())).willReturn(response);

            mockMvc.perform(post("/api/v1/aftercare/reports")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.reportContent").value("Completed all tasks"));
        }
    }
}
