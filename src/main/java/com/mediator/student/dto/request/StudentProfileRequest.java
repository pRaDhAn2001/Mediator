package com.mediator.student.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

import com.mediator.common.base.Address;
import com.mediator.common.base.Gender;
import com.mediator.common.base.PreferredMode;
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
public class StudentProfileRequest {

    @NotNull
    private Gender gender;

    @Valid
    @NotNull
    private Address address;

    @NotNull
    private PreferredMode preferredMode;

    @DecimalMin("0.0")
    @DecimalMax("100.0")
    private Double preferredRadius;

    @NotNull
    private Long classLevelId;

    @NotNull
    private Long boardId;

    @Size(max = 150)
    private String schoolName;

    @NotBlank
    @Size(max = 100)
    private String parentName;

    @Pattern(regexp = "^[6-9]\\d{9}$")
    private String parentPhone;

    @Email
    private String parentEmail;

    /**
     * If true
     * backend copies User.mobileNumber
     */
    private Boolean sameAsPrimaryPhone;

    /**
     * If true
     * backend copies User.email
     */
    private Boolean sameAsPrimaryEmail;

    @NotEmpty
    private List<Long> subjectIds;
}
