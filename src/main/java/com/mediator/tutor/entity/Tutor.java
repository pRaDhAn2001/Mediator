package com.mediator.tutor.entity;

import com.mediator.common.base.BaseEntity;
import com.mediator.common.base.Gender;
import com.mediator.common.base.Address;
import com.mediator.common.base.PreferredMode;

import java.math.BigDecimal;

import com.mediator.auth.entity.User;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tutors")
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tutor extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tutor_id")
    private Long tutorId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(length = 1000)
    private String description;

    @Embedded
    private Address address;

    @Embedded
    private AcademicProfile academicProfile;

    private Integer teachingExperienceYears;

    private Integer industryExperienceYears;

    private String currentOccupation;

    @Embedded
    private PreferredMode preferredMode;

    private Double preferredRadius;

    @Column(precision = 10, scale = 2)
    private BigDecimal salaryMin;

    @Column(precision = 10, scale = 2)
    private BigDecimal salaryMax;

    @Column(length = 1000)
    private String demoVideoUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ProfileStatus profileStatus = ProfileStatus.DRAFT;
}
