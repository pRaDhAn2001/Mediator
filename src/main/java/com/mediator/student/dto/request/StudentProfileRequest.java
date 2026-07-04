package com.mediator.student.dto.request;

import com.mediator.common.dto.AddressDto;
import com.mediator.common.base.PreferredMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentProfileRequest {

    private String gender;
    private AddressDto address;
    private PreferredMode preferredMode;
    private Double preferredRadius;
    private Long classLevelId;
    private Long boardId;
    private String schoolName;
    private String parentName;
    private String parentPhone;
    private String parentEmail;
}
