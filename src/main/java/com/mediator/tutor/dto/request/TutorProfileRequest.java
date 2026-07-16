package com.mediator.tutor.dto.request;

import java.math.BigDecimal;

import com.mediator.common.dto.AddressDto;
import com.mediator.common.base.Gender;
import com.mediator.common.dto.PreferredModeDto;
import com.mediator.tutor.dto.academics.AcademicProfileDto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class TutorProfileRequest {

    @NotNull
    private Gender gender;

    @NotBlank
    @Size(max = 1000)
    private String description;

    @Valid
    @NotNull
    private AddressDto address;

    @Valid
    @NotNull
    private AcademicProfileDto academicProfile;

    @NotNull
    @Min(0)
    @Max(60)
    private Integer teachingExperienceYears;

    @Min(0)
    @Max(60)
    private Integer industryExperienceYears;

    @Size(max = 100)
    private String currentOccupation;

    @NotNull
    private PreferredModeDto preferredMode;

    @DecimalMin("0.0")
    @DecimalMax("50.0")
    private Double preferredRadius;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal salaryMin;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal salaryMax;

    @Size(max = 1000)
    private String demoVideoUrl;

}
