package com.mediator.tutor.dto.request;

import java.util.List;

import com.mediator.common.dto.AddressDto;
import com.mediator.common.base.Gender;
import com.mediator.common.base.PreferredMode;
import com.mediator.tutor.dto.academics.AcademicProfileDto;
import com.mediator.tutor.dto.preference.TutorTeachingPreferenceDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TutorProfileRequest {

    private Gender gender;
    private String description;
    private AddressDto address;
    private Integer teachingExperienceYears;
    private Integer industryExperienceYears;
    private String currentOccupation;
    private PreferredMode preferredMode;
    private Double preferredRadius;
    private Double salaryMin;
    private Double salaryMax;
    private String demoVideoUrl;
    private AcademicProfileDto academicProfile;
    private List<TutorTeachingPreferenceDto> teachingPreferences;
}
