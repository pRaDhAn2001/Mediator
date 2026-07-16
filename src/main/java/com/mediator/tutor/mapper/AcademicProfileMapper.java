package com.mediator.tutor.mapper;

import org.springframework.stereotype.Component;

import com.mediator.tutor.dto.academics.AcademicProfileDto;
import com.mediator.tutor.entity.AcademicProfile;

@Component
public class AcademicProfileMapper {

    /**
     * DTO -> Entity
     */
    public AcademicProfile toEntity(AcademicProfileDto dto) {

        if (dto == null) {
            return null;
        }

        return AcademicProfile.builder()
                .class10Percentage(dto.getClass10Percentage())
                .class12Percentage(dto.getClass12Percentage())
                .highestQualification(dto.getHighestQualification())
                .degreeName(dto.getDegreeName())
                .specialization(dto.getSpecialization())
                .instituteName(dto.getInstituteName())
                .passingYear(dto.getPassingYear())
                .build();
    }

    /**
     * Entity -> DTO
     */
    public AcademicProfileDto toDto(AcademicProfile entity) {

        if (entity == null) {
            return null;
        }

        return AcademicProfileDto.builder()
                .class10Percentage(entity.getClass10Percentage())
                .class12Percentage(entity.getClass12Percentage())
                .highestQualification(entity.getHighestQualification())
                .degreeName(entity.getDegreeName())
                .specialization(entity.getSpecialization())
                .instituteName(entity.getInstituteName())
                .passingYear(entity.getPassingYear())
                .build();
    }
}