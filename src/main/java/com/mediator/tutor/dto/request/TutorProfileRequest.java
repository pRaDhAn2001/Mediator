package com.mediator.tutor.dto.request;

import java.math.BigDecimal;

import com.mediator.common.dto.AddressDto;
import com.mediator.common.base.Gender;
import com.mediator.common.dto.PreferredModeDto;
import com.mediator.tutor.dto.academics.AcademicProfileDto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Information required to create or update a tutor's professional profile.")
public class TutorProfileRequest {

    @NotNull
    @Schema(description = "Tutor's gender.", example = "MALE", requiredMode = Schema.RequiredMode.REQUIRED)
    private Gender gender;

    @NotBlank
    @Size(max = 1000)
    @Schema(description = "Professional introduction and teaching experience description.", example = "Experienced Mathematics tutor with a strong academic background.", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 1000)
    private String description;

    @Valid
    @NotNull
    @Schema(description = "Tutor's residential address.", requiredMode = Schema.RequiredMode.REQUIRED)
    private AddressDto address;

    @Valid
    @NotNull
    @Schema(description = "Tutor's academic profile.", requiredMode = Schema.RequiredMode.REQUIRED)
    private AcademicProfileDto academicProfile;

    @NotNull
    @Min(0)
    @Max(60)
    @Schema(description = "Number of years of teaching experience. Allowed range: 0 to 60.", example = "5", minimum = "0", maximum = "60", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer teachingExperienceYears;

    @Min(0)
    @Max(60)
    @Schema(description = "Number of years of industry experience. Allowed range: 0 to 60.", example = "3", minimum = "0", maximum = "60")
    private Integer industryExperienceYears;

    @Size(max = 100)
    @Schema(description = "Tutor's current occupation.", example = "Software Engineer", maxLength = 100)
    private String currentOccupation;

    @NotNull
    @Schema(description = "Preferred teaching modes.", requiredMode = Schema.RequiredMode.REQUIRED)
    private PreferredModeDto preferredMode;

    @DecimalMin("0.0")
    @DecimalMax("50.0")
    @Schema(description = "Preferred teaching/search radius. Allowed range: 0 to 50, in the application's configured unit.", example = "10.0", minimum = "0", maximum = "50")
    private Double preferredRadius;

    @NotNull
    @DecimalMin("0.0")
    @Schema(description = "Minimum expected salary per month.", example = "5000.00", minimum = "0", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal salaryMin;

    @NotNull
    @DecimalMin("0.0")
    @Schema(description = "Maximum expected salary per month.", example = "15000.00", minimum = "0", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal salaryMax;

    @Size(max = 1000)
    @Schema(description = "URL of the tutor's demo teaching video.", example = "https://drive.google.com/file/d/example", maxLength = 1000)
    private String demoVideoUrl;
}