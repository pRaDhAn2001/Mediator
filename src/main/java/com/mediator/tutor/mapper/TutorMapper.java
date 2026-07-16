package com.mediator.tutor.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.mediator.common.mapper.AddressMapper;
import com.mediator.common.mapper.PreferredModeMapper;
import com.mediator.tutor.dto.document.TutorDocumentDto;
import com.mediator.tutor.dto.preference.TutorTeachingPreferenceDto;
import com.mediator.tutor.dto.request.TutorProfileRequest;
import com.mediator.tutor.dto.response.TutorProfileResponse;
import com.mediator.tutor.entity.Tutor;
import com.mediator.tutor.entity.TutorDocument;
import com.mediator.tutor.entity.TutorTeachingPreference;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TutorMapper {

    private final AddressMapper addressMapper;

    private final PreferredModeMapper preferredModeMapper;

    private final AcademicProfileMapper academicProfileMapper;

    /**
     * Request DTO -> Entity
     */
    public void updateTutorEntity(
            Tutor tutor,
            TutorProfileRequest request) {

        tutor.setGender(request.getGender());

        tutor.setDescription(request.getDescription());

        tutor.setAddress(
                addressMapper.toEntity(
                        request.getAddress()));

        tutor.setAcademicProfile(
                academicProfileMapper.toEntity(
                        request.getAcademicProfile()));

        tutor.setTeachingExperienceYears(
                request.getTeachingExperienceYears());

        tutor.setIndustryExperienceYears(
                request.getIndustryExperienceYears());

        tutor.setCurrentOccupation(
                request.getCurrentOccupation());

        tutor.setPreferredMode(
                preferredModeMapper.toEntity(
                        request.getPreferredMode()));

        tutor.setPreferredRadius(
                request.getPreferredRadius());

        tutor.setSalaryMin(
                request.getSalaryMin());

        tutor.setSalaryMax(
                request.getSalaryMax());

        tutor.setDemoVideoUrl(
                request.getDemoVideoUrl());
    }

    /**
     * Entity -> Response DTO
     */
    public TutorProfileResponse toResponse(
            Tutor tutor,
            Integer profileCompletion,
            List<TutorDocument> documents) {

        return TutorProfileResponse.builder()

                .tutorId(tutor.getTutorId())

                .firstName(tutor.getUser().getFirstName())

                .lastName(tutor.getUser().getLastName())

                .email(tutor.getUser().getEmail())

                .mobileNumber(tutor.getUser().getMobileNumber())

                .gender(tutor.getGender())

                .description(tutor.getDescription())

                .address(
                        addressMapper.toDto(
                                tutor.getAddress()))

                .academicProfile(
                        academicProfileMapper.toDto(
                                tutor.getAcademicProfile()))

                .teachingExperienceYears(
                        tutor.getTeachingExperienceYears())

                .industryExperienceYears(
                        tutor.getIndustryExperienceYears())

                .currentOccupation(
                        tutor.getCurrentOccupation())

                .preferredMode(
                        preferredModeMapper.toDto(
                                tutor.getPreferredMode()))

                .preferredRadius(
                        tutor.getPreferredRadius())

                .salaryMin(
                        tutor.getSalaryMin())

                .salaryMax(
                        tutor.getSalaryMax())

                .demoVideoUrl(
                        tutor.getDemoVideoUrl())

                .verificationStatus(
                        tutor.getVerificationStatus())

                .profileStatus(
                        tutor.getProfileStatus())

                .profileCompletion(
                        profileCompletion)

                .documents(
                        mapDocuments(documents))

                .build();
    }

    /**
     * Tutor Documents
     */
    private List<TutorDocumentDto> mapDocuments(
            List<TutorDocument> documents) {

        if (documents == null) {
            return List.of();
        }

        return documents.stream()
                .map(this::toDocumentDto)
                .collect(Collectors.toList());
    }

    /**
     * Single Document
     */
    private TutorDocumentDto toDocumentDto(
            TutorDocument document) {

        return TutorDocumentDto.builder()
                .documentType(document.getDocumentType())
                .documentUrl(document.getDocumentUrl())
                .build();
    }

    public TutorTeachingPreferenceDto toPreferenceDto(
            TutorTeachingPreference preference) {

        return TutorTeachingPreferenceDto.builder()
                .subjectId(preference.getSubject().getId())
                .classLevelId(preference.getClassLevel().getId())
                .boardId(preference.getBoard().getId())
                .build();
    }

}