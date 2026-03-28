package com.k.medtour.domain.staff.entity;

import com.k.medtour.domain.admin.entity.Member;
import com.k.medtour.domain.staff.enums.StaffType;
import com.k.medtour.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.Map;

@Entity
@Table(name = "staff_profile", indexes = {
        @Index(name = "idx_staff_profile_member_id", columnList = "member_id"),
        @Index(name = "idx_staff_profile_staff_type", columnList = "staff_type")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StaffProfile extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "staff_type", nullable = false, length = 20)
    private StaffType staffType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "languages", columnDefinition = "jsonb")
    private List<String> languages;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "vehicle_info", columnDefinition = "jsonb")
    private Map<String, Object> vehicleInfo;

    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable = true;

    @Builder
    public StaffProfile(Member member, StaffType staffType, List<String> languages,
                        Map<String, Object> vehicleInfo, Boolean isAvailable) {
        this.member = member;
        this.staffType = staffType;
        this.languages = languages;
        this.vehicleInfo = vehicleInfo;
        this.isAvailable = isAvailable != null ? isAvailable : true;
    }

    public void updateProfile(StaffType staffType, List<String> languages,
                              Map<String, Object> vehicleInfo) {
        if (staffType != null) this.staffType = staffType;
        if (languages != null) this.languages = languages;
        if (vehicleInfo != null) this.vehicleInfo = vehicleInfo;
    }

    public void updateAvailability(boolean isAvailable) {
        this.isAvailable = isAvailable;
    }
}
