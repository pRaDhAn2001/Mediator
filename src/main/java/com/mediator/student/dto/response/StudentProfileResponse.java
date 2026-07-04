package com.mediator.student.dto.response;

import com.mediator.common.dto.AddressDto;
import com.mediator.master.dto.BoardResponse;
import com.mediator.master.dto.ClassLevelResponse;
import com.mediator.common.base.PreferredMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentProfileResponse {

    private Long studentId;
    private String firstName;
    private String lastName;
    private String email;
    private String mobileNumber;
    private String gender;
    private AddressDto address;
    private PreferredMode preferredMode;
    private Double preferredRadius;
    private ClassLevelResponse classLevel;
    private BoardResponse board;
    private String schoolName;
    private String parentName;
    private String parentPhone;
    private String parentEmail;
}
