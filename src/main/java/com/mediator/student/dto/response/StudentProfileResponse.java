package com.mediator.student.dto.response;

import java.util.List;

import com.mediator.common.base.Gender;
import com.mediator.common.dto.AddressDto;
import com.mediator.common.dto.PreferredModeDto;
import com.mediator.master.dto.SubjectResponse;

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
@Schema(description = "Student profile details returned by the API.")
public class StudentProfileResponse {

    @Schema(description = "Student's first name.", example = "Aarav")
    private String firstName;

    @Schema(description = "Student's last name.", example = "Sharma")
    private String lastName;

    @Schema(description = "Student account email.", example = "student@example.com")
    private String email;

    @Schema(description = "Student account mobile number.", example = "9876543210")
    private String mobileNumber;

    @Schema(description = "Student's gender.", example = "MALE")
    private Gender gender;

    @Schema(description = "Student's address.")
    private AddressDto address;

    @Schema(description = "Preferred teaching modes.")
    private PreferredModeDto preferredMode;

    @Schema(description = "Preferred search radius, in the unit used by the application.", example = "10.0")
    private Double preferredRadius;

    @Schema(description = "Class level ID.", example = "3")
    private Long classLevelId;

    @Schema(description = "Class standard represented by the class level.", example = "8")
    private Integer classStandard;

    @Schema(description = "Board ID.", example = "1")
    private Long boardId;

    @Schema(description = "Board name.", example = "CBSE")
    private String boardName;

    @Schema(description = "School name.", example = "ABC Public School")
    private String schoolName;

    @Schema(description = "Parent or guardian name.", example = "Rahul Sharma")
    private String parentName;

    @Schema(description = "Parent contact number.", example = "9876543211")
    private String parentPhone;

    @Schema(description = "Parent email address.", example = "parent@example.com")
    private String parentEmail;

    @Schema(description = "Preferred subjects.")
    private List<SubjectResponse> subjects;

    @Schema(description = "Profile completion percentage.", example = "85", minimum = "0", maximum = "100")
    private Integer profileCompletion;
}