package com.mediator.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Address and geographical location information")
public class AddressDto {

    @Schema(description = "Street or house address", example = "Flat 302, Green Residency")
    private String street;

    @Schema(description = "City name", example = "Hyderabad")
    private String city;

    @Schema(description = "District name", example = "Rangareddy")
    private String district;

    @Schema(description = "State name", example = "Telangana")
    private String state;

    @Schema(description = "Postal PIN code", example = "500032")
    private String zipCode;

    @Schema(description = "Geographical latitude coordinate", example = "17.4401")
    private Double latitude;

    @Schema(description = "Geographical longitude coordinate", example = "78.3489")
    private Double longitude;
}