package com.mediator.matching.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TutorActionRequest {

    @NotEmpty(message = "At least one subject must be selected")
    private List<Long> acceptedSubjectIds;
}