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

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Tutor profile information returned by the API.")
public class TutorProfileResponse {

    @Schema(description = "Unique tutor ID.", example = "101")
    private Long tutorId;

    @Schema(description = "Tutor's first name.", example = "Rahul")
    private String firstName;

    @Schema(description = "Tutor's last name.", example = "Sharma")
    private String lastName;

    @Schema(description = "Tutor account email.", example = "tutor@example.com")
    private String email;

    @Schema(description = "Tutor account mobile number.", example = "9876543210")
    private String mobileNumber;

    @Schema(description = "Tutor's gender.", example = "MALE")
    private Gender gender;

    @Schema(description = "Tutor's professional description.")
    private String description;

    @Schema(description = "Tutor's address.")
    private AddressDto address;

    @Schema(description = "Tutor's academic profile.")
    private AcademicProfileDto academicProfile;

    @Schema(description = "Years of teaching experience.", example = "5")
    private Integer teachingExperienceYears;

    @Schema(description = "Years of industry experience.", example = "3")
    private Integer industryExperienceYears;

    @Schema(description = "Tutor's current occupation.", example = "Software Engineer")
    private String currentOccupation;

    @Schema(description = "Tutor's preferred teaching modes.")
    private PreferredModeDto preferredMode;

    @Schema(description = "Preferred teaching/search radius.", example = "10.0")
    private Double preferredRadius;

    @Schema(description = "Minimum expected monthly salary.", example = "5000.00")
    private BigDecimal salaryMin;

    @Schema(description = "Maximum expected monthly salary.", example = "15000.00")
    private BigDecimal salaryMax;

    @ArraySchema(
            arraySchema = @Schema(description = "Documents submitted by the tutor."),
            schema = @Schema(implementation = TutorDocumentDto.class)
    )
    private List<TutorDocumentDto> documents;

    @Schema(description = "Tutor's demo teaching video URL.")
    private String demoVideoUrl;

    @Schema(description = "Tutor's document verification status.", example = "PENDING")
    private VerificationStatus verificationStatus;

    @Schema(description = "Tutor's profile status.", example = "DRAFT")
    private ProfileStatus profileStatus;

    @Schema(
            description = "Profile completion percentage.",
            example = "85",
            minimum = "0",
            maximum = "100"
    )
    private Integer profileCompletion;
}