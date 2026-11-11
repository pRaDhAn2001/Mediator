package com.mediator.tutor.dto.request;

import java.util.List;

import com.mediator.tutor.dto.preference.TutorTeachingPreferenceDto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

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
@Schema(description = "Request containing the tutor's selected teaching preferences.")
public class TutorTeachingPreferenceRequest {

    @NotEmpty
    @ArraySchema(arraySchema = @Schema(description = "Teaching preference entries. At least one preference is required."), schema = @Schema(implementation = TutorTeachingPreferenceDto.class))
    private List<TutorTeachingPreferenceDto> preferences;
}