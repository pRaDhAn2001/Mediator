package com.mediator.tutor.dto.response;

import java.math.BigDecimal;
import java.util.List;

import com.mediator.common.base.Gender;
import com.mediator.common.dto.AddressDto;
import com.mediator.common.dto.PreferredModeDto;
import com.mediator.tutor.dto.academics.AcademicProfileDto;
import com.mediator.tutor.dto.document.TutorDocumentDto;
import com.mediator.tutor.entity.ProfileStatus;
import com.mediator.tutor.entity.VerificationStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TutorProfileResponse {

    private Long tutorId;
    private String firstName;
    private String lastName;
    private String email;
    private String mobileNumber;
    private Gender gender;
    private String description;
    private AddressDto address;
    private AcademicProfileDto academicProfile;
    private Integer teachingExperienceYears;
    private Integer industryExperienceYears;
    private String currentOccupation;
    private PreferredModeDto preferredMode;
    private Double preferredRadius;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private List<TutorDocumentDto> documents;
    private String demoVideoUrl;
    private VerificationStatus verificationStatus;
    private ProfileStatus profileStatus;
    private Integer profileCompletion;
}
