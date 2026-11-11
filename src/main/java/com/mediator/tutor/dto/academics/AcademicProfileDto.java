package com.mediator.tutor.dto.academics;

import com.mediator.tutor.entity.HighestQualification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Getter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Tutor's academic qualifications and educational background.")
public class AcademicProfileDto {

    @Schema(description = "Percentage obtained in Class 10.", example = "88.5", minimum = "0", maximum = "100")
    private Double class10Percentage;

    @Schema(description = "Percentage obtained in Class 12.", example = "85.2", minimum = "0", maximum = "100")
    private Double class12Percentage;

    @Schema(description = "Highest educational qualification.", example = "BACHELORS")
    private HighestQualification highestQualification;

    @Schema(description = "Name of the degree or qualification.", example = "Bachelor of Technology")
    private String degreeName;

    @Schema(description = "Area of specialization.", example = "Computer Science and Engineering")
    private String specialization;

    @Schema(description = "Name of the educational institution.", example = "ABC University")
    private String instituteName;

    @Schema(description = "Year of passing.", example = "2022")
    private Integer passingYear;
}