package com.mediator.tutor.dto.academics;

import com.mediator.tutor.entity.HighestQualification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcademicProfileDto {

    private Double class10Percentage;
    private Double class12Percentage;
    private HighestQualification highestQualification;
    private String degreeName;
    private String specialization;
    private String instituteName;
    private Integer passingYear;
}
