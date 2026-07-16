package com.mediator.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressDto {

    private String street;
    private String city;
    private String district;
    private String state;
    private String zipCode;
    private Double latitude;
    private Double longitude;
}
