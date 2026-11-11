package com.mediator.student.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import com.mediator.common.base.Gender;
import com.mediator.common.dto.PreferredModeDto;
import com.mediator.common.dto.AddressDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Information used to create or update a student's profile.")
public class StudentProfileRequest {

    @NotNull
    @Schema(description = "Student's gender.", example = "MALE", requiredMode = Schema.RequiredMode.REQUIRED)
    private Gender gender;

    @Valid
    @NotNull
    @Schema(description = "Student's address details.", requiredMode = Schema.RequiredMode.REQUIRED)
    private AddressDto address;

    @NotNull
    @Schema(description = "Preferred teaching modes.", requiredMode = Schema.RequiredMode.REQUIRED)
    private PreferredModeDto preferredMode;

    @DecimalMin("0.0")
    @DecimalMax("100.0")
    @Schema(description = "Preferred search radius, in the unit used by the application. Must be between 0 and 100.", example = "10.0", minimum = "0", maximum = "100")
    private Double preferredRadius;

    @NotNull
    @Schema(description = "ID of the student's class level.", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long classLevelId;

    @NotNull
    @Schema(description = "ID of the education board.", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long boardId;

    @Size(max = 150)
    @Schema(description = "Name of the student's school.", example = "ABC Public School", maxLength = 150)
    private String schoolName;

    @NotBlank
    @Size(max = 100)
    @Schema(description = "Parent or guardian's name.", example = "Rahul Sharma", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 100)
    private String parentName;

    @Pattern(regexp = "^[6-9]\\d{9}$")
    @Schema(description = "Parent contact number. If supplied, it must be a valid 10-digit Indian mobile number.", example = "9876543210", pattern = "^[6-9]\\d{9}$")
    private String parentPhone;

    @Email
    @Schema(description = "Parent or guardian's email address.", example = "parent@example.com")
    private String parentEmail;

    /**
     * If true backend copies User.mobileNumber.
     */
    @Schema(description = "When true, the backend copies the primary user's mobile number into the parent contact field.", example = "false")
    private Boolean sameAsPrimaryPhone;

    /**
     * If true backend copies User.email.
     */
    @Schema(description = "When true, the backend copies the primary user's email into the parent contact field.", example = "false")
    private Boolean sameAsPrimaryEmail;
}