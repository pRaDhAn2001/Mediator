package com.mediator.tutor.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Embeddable
public class AcademicProfile {

    private Double class10Percentage;

    private Double class12Percentage;

    @Enumerated(EnumType.STRING)
    private HighestQualification highestQualification;

    private String degreeName;

    private String specialization;

    private String instituteName;

    private Integer passingYear;
}
