package com.k.medtour.domain.patient.entity;

import com.k.medtour.domain.admin.entity.Member;
import com.k.medtour.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "emergency_contact")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmergencyContact extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "relationship", nullable = false, length = 50)
    private String relationship;

    @Column(name = "phone", nullable = false, length = 30)
    private String phone;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary = false;

    @Builder
    public EmergencyContact(Member member, String name, String relationship,
                            String phone, String email, Boolean isPrimary) {
        this.member = member;
        this.name = name;
        this.relationship = relationship;
        this.phone = phone;
        this.email = email;
        this.isPrimary = isPrimary != null ? isPrimary : false;
    }

    public void update(String name, String relationship, String phone,
                       String email, Boolean isPrimary) {
        this.name = name;
        this.relationship = relationship;
        this.phone = phone;
        this.email = email;
        if (isPrimary != null) this.isPrimary = isPrimary;
    }
}
