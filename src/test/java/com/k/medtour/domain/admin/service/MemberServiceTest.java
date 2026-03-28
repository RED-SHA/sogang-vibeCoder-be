package com.k.medtour.domain.admin.service;

import com.k.medtour.domain.admin.dto.*;
import com.k.medtour.domain.admin.entity.AgencyProfile;
import com.k.medtour.domain.admin.entity.Member;
import com.k.medtour.domain.admin.entity.Role;
import com.k.medtour.domain.admin.repository.AgencyProfileRepository;
import com.k.medtour.domain.admin.repository.MemberRepository;
import com.k.medtour.domain.staff.entity.StaffProfile;
import com.k.medtour.domain.staff.enums.StaffType;
import com.k.medtour.domain.staff.repository.StaffProfileRepository;
import com.k.medtour.global.exception.BusinessException;
import com.k.medtour.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @InjectMocks
    private MemberService memberService;

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private StaffProfileRepository staffProfileRepository;
    @Mock
    private AgencyProfileRepository agencyProfileRepository;

    @Nested
    @DisplayName("실무자 프로필 조회")
    class GetStaffProfileTest {

        @Test
        @DisplayName("성공 - 실무자 프로필이 정상적으로 조회된다")
        void getStaffProfile_success() {
            // Given
            Long memberId = 1L;
            Role staffRole = Role.builder().name("ROLE_STAFF").description("실무자").build();
            Member member = Member.builder()
                    .email("driver@test.com")
                    .name("김기사")
                    .role(staffRole)
                    .phone("+82-10-1234-5678")
                    .build();

            StaffProfile staffProfile = StaffProfile.builder()
                    .member(member)
                    .staffType(StaffType.DRIVER)
                    .languages(List.of("ko", "en"))
                    .vehicleInfo(Map.of("type", "SEDAN", "plateNumber", "서울 12가 3456"))
                    .isAvailable(true)
                    .build();

            given(staffProfileRepository.findByMemberIdWithMember(memberId))
                    .willReturn(Optional.of(staffProfile));

            // When
            StaffProfileResponse response = memberService.getStaffProfile(memberId);

            // Then
            assertThat(response.name()).isEqualTo("김기사");
            assertThat(response.staffType()).isEqualTo("DRIVER");
            assertThat(response.languages()).containsExactly("ko", "en");
        }

        @Test
        @DisplayName("실패 - 실무자 프로필이 없으면 예외가 발생한다")
        void getStaffProfile_fail_notFound() {
            // Given
            Long memberId = 99L;
            given(staffProfileRepository.findByMemberIdWithMember(memberId))
                    .willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> memberService.getStaffProfile(memberId))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.STAFF_PROFILE_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("실무자 프로필 수정")
    class UpdateStaffProfileTest {

        @Test
        @DisplayName("성공 - 실무자 프로필이 정상적으로 수정된다")
        void updateStaffProfile_success() {
            // Given
            Long memberId = 1L;
            Role staffRole = Role.builder().name("ROLE_STAFF").description("실무자").build();
            Member member = Member.builder()
                    .email("driver@test.com")
                    .name("김기사")
                    .role(staffRole)
                    .phone("+82-10-1234-5678")
                    .build();

            StaffProfile staffProfile = StaffProfile.builder()
                    .member(member)
                    .staffType(StaffType.DRIVER)
                    .languages(List.of("ko", "en"))
                    .vehicleInfo(Map.of("type", "SEDAN"))
                    .isAvailable(true)
                    .build();

            given(staffProfileRepository.findByMemberIdWithMember(memberId))
                    .willReturn(Optional.of(staffProfile));

            StaffProfileUpdateRequest request = new StaffProfileUpdateRequest(
                    "김기사 수정", "+82-10-9999-8888", List.of("ko", "en", "zh"), null,
                    Map.of("type", "SUV", "plateNumber", "서울 99가 1234")
            );

            // When
            StaffProfileResponse response = memberService.updateStaffProfile(memberId, request);

            // Then
            assertThat(response.name()).isEqualTo("김기사 수정");
            assertThat(response.languages()).containsExactly("ko", "en", "zh");
        }

        @Test
        @DisplayName("실패 - 실무자 프로필이 없으면 수정 시 예외가 발생한다")
        void updateStaffProfile_fail_notFound() {
            // Given
            Long memberId = 99L;
            StaffProfileUpdateRequest request = new StaffProfileUpdateRequest("name", null, null, null, null);
            given(staffProfileRepository.findByMemberIdWithMember(memberId)).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> memberService.updateStaffProfile(memberId, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.STAFF_PROFILE_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("에이전시 프로필 조회")
    class GetAgencyProfileTest {

        @Test
        @DisplayName("성공 - 에이전시 프로필이 정상적으로 조회된다")
        void getAgencyProfile_success() {
            // Given
            AgencyProfile profile = AgencyProfile.builder()
                    .name("서울 뷰티 클리닉")
                    .licenseNumber("H-2025-001234")
                    .licenseVerified(true)
                    .address("서울시 강남구")
                    .phone("+82-2-1234-5678")
                    .build();

            given(agencyProfileRepository.findAll()).willReturn(List.of(profile));

            // When
            AgencyProfileResponse response = memberService.getAgencyProfile();

            // Then
            assertThat(response.name()).isEqualTo("서울 뷰티 클리닉");
            assertThat(response.licenseVerified()).isTrue();
        }

        @Test
        @DisplayName("실패 - 에이전시 프로필이 없으면 예외가 발생한다")
        void getAgencyProfile_fail_notFound() {
            // Given
            given(agencyProfileRepository.findAll()).willReturn(List.of());

            // When & Then
            assertThatThrownBy(() -> memberService.getAgencyProfile())
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.AGENCY_PROFILE_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("라이선스 검증")
    class LicenseVerifyTest {

        @Test
        @DisplayName("성공 - 라이선스 검증 정보가 조회된다")
        void verifyLicense_success() {
            // Given
            AgencyProfile profile = AgencyProfile.builder()
                    .name("서울 뷰티 클리닉")
                    .licenseNumber("H-2025-001234")
                    .licenseVerified(true)
                    .build();

            given(agencyProfileRepository.findAll()).willReturn(List.of(profile));

            // When
            LicenseVerifyResponse response = memberService.verifyLicense();

            // Then
            assertThat(response.licenseNumber()).isEqualTo("H-2025-001234");
            assertThat(response.licenseVerified()).isTrue();
        }

        @Test
        @DisplayName("실패 - 에이전시가 없으면 예외가 발생한다")
        void verifyLicense_fail_notFound() {
            // Given
            given(agencyProfileRepository.findAll()).willReturn(List.of());

            // When & Then
            assertThatThrownBy(() -> memberService.verifyLicense())
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.AGENCY_PROFILE_NOT_FOUND);
        }
    }
}
