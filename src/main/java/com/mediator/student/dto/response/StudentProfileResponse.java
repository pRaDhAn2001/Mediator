package com.mediator.student.dto.response;

import java.util.List;

import com.mediator.common.base.Address;
import com.mediator.common.base.Gender;
import com.mediator.common.base.PreferredMode;
import com.mediator.master.dto.SubjectResponse;
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
public class StudentProfileResponse {

    private String firstName;

    private String lastName;

    private String email;

    private String mobileNumber;

    private Gender gender;

    private Address address;

    private PreferredMode preferredMode;

    private Double preferredRadius;

    private Long classLevelId;

    private Integer classStandard;

    private Long boardId;

    private String boardName;

    private String schoolName;

    private String parentName;

    private String parentPhone;

    private String parentEmail;

    private List<SubjectResponse> subjects;

    private Integer profileCompletion;
}
