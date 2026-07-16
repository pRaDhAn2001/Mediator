package com.mediator.common.mapper;

import org.springframework.stereotype.Component;

import com.mediator.common.base.PreferredMode;
import com.mediator.common.dto.PreferredModeDto;

@Component
public class PreferredModeMapper {

    /**
     * DTO -> Entity
     */
    public PreferredMode toEntity(PreferredModeDto dto) {

        if (dto == null) {
            return null;
        }

        return PreferredMode.builder()
                .online(dto.isOnline())
                .tutorHome(dto.isTutorHome())
                .studentHome(dto.isStudentHome())
                .build();
    }

    /**
     * Entity -> DTO
     */
    public PreferredModeDto toDto(PreferredMode entity) {

        if (entity == null) {
            return null;
        }

        return PreferredModeDto.builder()
                .online(entity.isOnline())
                .tutorHome(entity.isTutorHome())
                .studentHome(entity.isStudentHome())
                .build();
    }
}