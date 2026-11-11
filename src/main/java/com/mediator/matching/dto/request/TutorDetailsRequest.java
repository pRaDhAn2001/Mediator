package com.mediator.matching.dto.request;

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
@Schema(description = "Location coordinates used for tutor details or distance-related processing")
public class TutorDetailsRequest {

    @Schema(description = "Latitude coordinate", example = "17.4401")
    private Double latitude;

    @Schema(description = "Longitude coordinate", example = "78.3489")
    private Double longitude;
}