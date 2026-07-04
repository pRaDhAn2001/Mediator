package com.mediator.tutor.entity;

import com.mediator.common.base.BaseEntity;
import com.mediator.common.base.Gender;
import com.mediator.common.base.Address;
import com.mediator.common.base.PreferredMode;
import com.mediator.auth.entity.User;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tutors")
@Data
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

    @Column(name = "salary_min")
    private Double salaryMin;

    @Column(name = "salary_max")
    private Double salaryMax;

    @Column(length = 1000)
    private String demoVideoUrl;

    @Column(name = "verification_status", nullable = false)
    @Builder.Default
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @Column(name = "profile_status", nullable = false)
    @Builder.Default
    private ProfileStatus profileStatus = ProfileStatus.DRAFT;
}
