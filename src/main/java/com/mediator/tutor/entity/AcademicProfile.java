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

    /*
     * Example:
     * B.Tech
     * B.Sc
     * M.Tech
     * MBA
     * Ph.D
     */
    private String degreeName;

    /*
     * Example:
     * Computer Science
     * Mathematics
     * Physics
     */
    private String specialization;

    /*
     * School or University
     */
    private String instituteName;

    /*
     * Graduation / Passing Year
     */
    private Integer passingYear;
}
