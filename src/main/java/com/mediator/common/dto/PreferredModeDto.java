package com.mediator.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Preferred teaching or learning modes")
public class PreferredModeDto {

    @Builder.Default
    @Schema(description = "Indicates whether online teaching or learning is preferred", example = "true", defaultValue = "false")
    private boolean online = false;

    @Builder.Default
    @Schema(description = "Indicates whether the tutor's home is the preferred teaching location", example = "false", defaultValue = "false")
    private boolean tutorHome = false;

    @Builder.Default
    @Schema(description = "Indicates whether the student's home is the preferred teaching location", example = "true", defaultValue = "false")
    private boolean studentHome = false;
}