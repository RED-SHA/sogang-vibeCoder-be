package com.k.medtour.domain.aftercare.service;

import com.k.medtour.domain.aftercare.dto.AftercareGuideCreateRequest;
import com.k.medtour.domain.aftercare.dto.AftercareGuideResponse;
import com.k.medtour.domain.aftercare.dto.InvoiceCreateRequest;
import com.k.medtour.domain.aftercare.dto.InvoiceResponse;
import com.k.medtour.domain.aftercare.dto.StaffReportCreateRequest;
import com.k.medtour.domain.aftercare.dto.StaffReportResponse;
import com.k.medtour.domain.aftercare.entity.AftercareGuide;
import com.k.medtour.domain.aftercare.entity.Invoice;
import com.k.medtour.domain.aftercare.entity.StaffReport;
import com.k.medtour.domain.aftercare.repository.AftercareGuideRepository;
import com.k.medtour.domain.aftercare.repository.InvoiceRepository;
import com.k.medtour.domain.aftercare.repository.StaffReportRepository;
import com.k.medtour.domain.journey.entity.Journey;
import com.k.medtour.domain.journey.repository.JourneyRepository;
import com.k.medtour.global.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AftercareServiceTest {

    @InjectMocks
    private AftercareService aftercareService;

    @Mock
    private AftercareGuideRepository guideRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private StaffReportRepository staffReportRepository;

    @Mock
    private JourneyRepository journeyRepository;

    @Nested
    @DisplayName("createGuide")
    class CreateGuideTest {

        @Test
        @DisplayName("성공 - 가이드를 생성한다")
        void createGuide_success() {
            // Given
            AftercareGuideCreateRequest request = new AftercareGuideCreateRequest(
                    1L, "Post-Surgery Guide", "Follow these instructions",
                    Map.of("day1", "rest", "day2", "check-up")
            );
            given(journeyRepository.findByIdAndDeletedAtIsNull(1L))
                    .willReturn(Optional.of(Journey.builder().build()));
            given(guideRepository.existsByJourneyIdAndDeletedAtIsNull(1L)).willReturn(false);
            given(guideRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

            // When
            AftercareGuideResponse result = aftercareService.createGuide(request);

            // Then
            assertThat(result.title()).isEqualTo("Post-Surgery Guide");
            assertThat(result.journeyId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("실패 - 여정이 없으면 예외 발생")
        void createGuide_journeyNotFound() {
            // Given
            AftercareGuideCreateRequest request = new AftercareGuideCreateRequest(
                    999L, "Guide", "Content", null);
            given(journeyRepository.findByIdAndDeletedAtIsNull(999L)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> aftercareService.createGuide(request))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("실패 - 이미 존재하면 예외 발생")
        void createGuide_alreadyExists() {
            // Given
            AftercareGuideCreateRequest request = new AftercareGuideCreateRequest(
                    1L, "Guide", "Content", null);
            given(journeyRepository.findByIdAndDeletedAtIsNull(1L))
                    .willReturn(Optional.of(Journey.builder().build()));
            given(guideRepository.existsByJourneyIdAndDeletedAtIsNull(1L)).willReturn(true);

            // When & Then
            assertThatThrownBy(() -> aftercareService.createGuide(request))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("getGuide")
    class GetGuideTest {

        @Test
        @DisplayName("성공 - 가이드를 조회한다")
        void getGuide_success() {
            // Given
            AftercareGuide guide = AftercareGuide.builder()
                    .journeyId(1L).title("Guide").content("Content").build();
            given(guideRepository.findByJourneyIdAndDeletedAtIsNull(1L))
                    .willReturn(Optional.of(guide));

            // When
            AftercareGuideResponse result = aftercareService.getGuide(1L);

            // Then
            assertThat(result.title()).isEqualTo("Guide");
        }

        @Test
        @DisplayName("실패 - 가이드가 없으면 예외 발생")
        void getGuide_notFound() {
            // Given
            given(guideRepository.findByJourneyIdAndDeletedAtIsNull(999L))
                    .willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> aftercareService.getGuide(999L))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("createInvoice")
    class CreateInvoiceTest {

        @Test
        @DisplayName("성공 - 인보이스를 생성한다")
        void createInvoice_success() {
            // Given
            InvoiceCreateRequest request = new InvoiceCreateRequest(
                    1L, 2L, "USD", LocalDate.now().plusDays(30),
                    List.of(
                            new InvoiceCreateRequest.InvoiceItemRequest("Surgery", new BigDecimal("5000"), 1),
                            new InvoiceCreateRequest.InvoiceItemRequest("Transport", new BigDecimal("200"), 3)
                    )
            );
            given(journeyRepository.findByIdAndDeletedAtIsNull(1L))
                    .willReturn(Optional.of(Journey.builder().build()));
            given(invoiceRepository.existsByJourneyIdAndDeletedAtIsNull(1L)).willReturn(false);
            given(invoiceRepository.countAllActive()).willReturn(0L);
            given(invoiceRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

            // When
            InvoiceResponse result = aftercareService.createInvoice(request);

            // Then
            assertThat(result.currency()).isEqualTo("USD");
            assertThat(result.items()).hasSize(2);
            assertThat(result.invoiceNumber()).startsWith("INV-");
        }

        @Test
        @DisplayName("실패 - 이미 존재하면 예외 발생")
        void createInvoice_alreadyExists() {
            // Given
            InvoiceCreateRequest request = new InvoiceCreateRequest(
                    1L, 2L, "USD", null,
                    List.of(new InvoiceCreateRequest.InvoiceItemRequest("Test", new BigDecimal("100"), 1))
            );
            given(journeyRepository.findByIdAndDeletedAtIsNull(1L))
                    .willReturn(Optional.of(Journey.builder().build()));
            given(invoiceRepository.existsByJourneyIdAndDeletedAtIsNull(1L)).willReturn(true);

            // When & Then
            assertThatThrownBy(() -> aftercareService.createInvoice(request))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("getMyInvoices")
    class GetMyInvoicesTest {

        @Test
        @DisplayName("성공 - 내 인보이스를 반환한다")
        void getMyInvoices_success() {
            // Given
            Invoice invoice = Invoice.builder()
                    .journeyId(1L).patientId(2L).invoiceNumber("INV-20260328-0001")
                    .currency("USD").subtotal(BigDecimal.valueOf(100))
                    .tax(BigDecimal.valueOf(10)).totalAmount(BigDecimal.valueOf(110))
                    .build();
            given(invoiceRepository.findByPatientIdWithItems(2L)).willReturn(List.of(invoice));

            // When
            List<InvoiceResponse> result = aftercareService.getMyInvoices(2L);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).invoiceNumber()).isEqualTo("INV-20260328-0001");
        }
    }

    @Nested
    @DisplayName("createReport")
    class CreateReportTest {

        @Test
        @DisplayName("성공 - 리포트를 생성한다")
        void createReport_success() {
            // Given
            StaffReportCreateRequest request = new StaffReportCreateRequest(
                    1L, "All tasks completed successfully.", 8.0);
            given(journeyRepository.findByIdAndDeletedAtIsNull(1L))
                    .willReturn(Optional.of(Journey.builder().build()));
            given(staffReportRepository.existsByJourneyIdAndStaffIdAndDeletedAtIsNull(1L, 5L))
                    .willReturn(false);
            given(staffReportRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

            // When
            StaffReportResponse result = aftercareService.createReport(request, 5L);

            // Then
            assertThat(result.reportContent()).isEqualTo("All tasks completed successfully.");
            assertThat(result.workHours()).isEqualTo(8.0);
        }

        @Test
        @DisplayName("실패 - 이미 리포트가 있으면 예외 발생")
        void createReport_alreadyExists() {
            // Given
            StaffReportCreateRequest request = new StaffReportCreateRequest(
                    1L, "Report", 8.0);
            given(journeyRepository.findByIdAndDeletedAtIsNull(1L))
                    .willReturn(Optional.of(Journey.builder().build()));
            given(staffReportRepository.existsByJourneyIdAndStaffIdAndDeletedAtIsNull(1L, 5L))
                    .willReturn(true);

            // When & Then
            assertThatThrownBy(() -> aftercareService.createReport(request, 5L))
                    .isInstanceOf(BusinessException.class);
        }
    }
}
