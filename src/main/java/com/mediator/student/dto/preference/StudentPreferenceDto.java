package com.mediator.student.dto.preference;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

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
@Schema(description = "Preferred subject IDs selected by a student.")
public class StudentPreferenceDto {

    @NotEmpty(message = "Please select at least one subject.")
    @ArraySchema(arraySchema = @Schema(description = "At least one subject ID is required."), schema = @Schema(type = "integer", format = "int64", example = "1"))
    private List<Long> subjectIds;
}