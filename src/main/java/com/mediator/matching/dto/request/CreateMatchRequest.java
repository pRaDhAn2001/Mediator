package com.mediator.matching.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for creating a student-tutor match request")
public class CreateMatchRequest {

    @NotNull(message = "Tutor ID is required")
    @Schema(description = "ID of the tutor receiving the request", example = "12", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long tutorId;

    @NotEmpty(message = "At least one subject is required")
    @Size(max = 20, message = "Maximum 20 subjects can be requested")
    @ArraySchema(schema = @Schema(description = "Subject ID", example = "1"), arraySchema = @Schema(description = "Subjects requested by the student"))
    private List<Long> subjectIds;

    @Size(max = 500, message = "Message cannot exceed 500 characters")
    @Schema(description = "Optional message to the tutor", example = "I am looking for mathematics tuition.")
    private String message;
}