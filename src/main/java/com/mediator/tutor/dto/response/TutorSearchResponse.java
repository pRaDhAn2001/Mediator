package com.mediator.tutor.dto.response;

import java.util.List;

import com.mediator.common.base.PreferredMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TutorSearchResponse {

    private Long tutorId;
    private String tutorName;
    private String city;
    private Integer teachingExperienceYears;
    private Double salaryMin;
    private Double salaryMax;
    private PreferredMode preferredMode;
    private Boolean verified;
    private Double distance;
    private Integer matchedSubjectCount;
    private List<String> matchedSubjects;
}
