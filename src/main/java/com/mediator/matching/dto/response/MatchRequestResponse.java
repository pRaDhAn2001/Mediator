package com.mediator.matching.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

import com.mediator.matching.entity.MatchRequestStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Match request details returned to the student or tutor")
public class MatchRequestResponse {

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

    @Schema(description = "Student phone number. Visible only to the tutor; returned as null to the student.", example = "9876543210", accessMode = Schema.AccessMode.READ_ONLY)
    private String studentPhone;

    @Schema(description = "Student email address. Visible only to the tutor; returned as null to the student.", example = "student@example.com", accessMode = Schema.AccessMode.READ_ONLY)
    private String studentEmail;

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