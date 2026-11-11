package com.mediator.matching.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for selecting subjects during partial finalization")
public class TutorActionRequest {

    @NotEmpty(message = "At least one subject must be selected")
    @ArraySchema(schema = @Schema(description = "Accepted subject ID", example = "1"), arraySchema = @Schema(description = "Subject IDs accepted by the tutor"))
    private List<Long> acceptedSubjectIds;
}