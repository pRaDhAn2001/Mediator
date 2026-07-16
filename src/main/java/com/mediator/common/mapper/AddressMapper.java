package com.mediator.common.mapper;

import org.springframework.stereotype.Component;

import com.mediator.common.base.Address;
import com.mediator.common.dto.AddressDto;

@Component
public class AddressMapper {

    public Address toEntity(AddressDto dto) {

        if (dto == null) {
            return null;
        }

        return Address.builder()
                .street(dto.getStreet())
                .city(dto.getCity())
                .district(dto.getDistrict())
                .state(dto.getState())
                .zipCode(dto.getZipCode())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .build();
    }

    public AddressDto toDto(Address entity) {

        if (entity == null) {
            return null;
        }

        return AddressDto.builder()
                .street(entity.getStreet())
                .city(entity.getCity())
                .district(entity.getDistrict())
                .state(entity.getState())
                .zipCode(entity.getZipCode())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .build();
    }
}
