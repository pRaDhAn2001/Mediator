package com.mediator.matching.dto.response;

import com.mediator.matching.entity.SubjectRequestStatus;

import lombok.*;

import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Subject-level status within a match request")
public class MatchRequestSubjectResponse {

    @Schema(description = "Subject ID", example = "1")
    private Long subjectId;

    @Schema(description = "Subject name", example = "Mathematics")
    private String subjectName;

    @Schema(description = "Current status of this subject request", example = "ACCEPTED")
    private SubjectRequestStatus status;
}