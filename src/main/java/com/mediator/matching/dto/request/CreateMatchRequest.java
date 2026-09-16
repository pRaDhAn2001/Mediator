package com.mediator.matching.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMatchRequest {

    @NotNull(message = "Tutor ID is required")
    private Long tutorId;

    @NotEmpty(message = "At least one subject is required")
    @Size(max = 20, message = "Maximum 20 subjects can be requested")
    private List<Long> subjectIds;

    @Size(max = 500, message = "Message cannot exceed 500 characters")
    private String message;
}
