package com.mediator.matching.dto.response;

import com.mediator.matching.entity.MatchRequestStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Match request information displayed in the student dashboard")
public class StudentMatchRequestResponse {

    @Schema(description = "Unique match request ID", example = "101")
    private Long requestId;

    @Schema(description = "Student ID", example = "10")
    private Long studentId;

    @Schema(description = "Student's name", example = "Rahul Sharma")
    private String studentName;

    @Schema(description = "Tutor ID", example = "12")
    private Long tutorId;

    @Schema(description = "Tutor's name", example = "Amit Kumar")
    private String tutorName;

    @Schema(description = "Current status of the match request", example = "REQUESTED")
    private MatchRequestStatus status;

    @Schema(description = "Message provided with the request", example = "I am looking for mathematics tuition.")
    private String message;

    @Schema(description = "Date and time when the request was created", example = "2026-11-10T10:30:00")
    private LocalDateTime requestedAt;

    @Schema(description = "Date and time when the tutor connected to the request", example = "2026-11-10T11:00:00")
    private LocalDateTime connectedAt;

    @Schema(description = "Date and time when the request was finalized", example = "2026-11-10T12:00:00")
    private LocalDateTime finalizedAt;

    @ArraySchema(schema = @Schema(implementation = MatchRequestSubjectResponse.class))
    private List<MatchRequestSubjectResponse> subjects;
}