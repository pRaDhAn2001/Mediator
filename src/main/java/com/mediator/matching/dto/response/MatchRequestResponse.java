package com.mediator.matching.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

import com.mediator.matching.entity.MatchRequestStatus;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchRequestResponse {

    private Long requestId;

    private Long studentId;
    private String studentName;

    private Long tutorId;
    private String tutorName;

    /*
     * Visible only to tutor.
     * Student will always receive null.
     */
    private String studentPhone;
    private String studentEmail;

    /*
     * Do NOT add tutorPhone/tutorEmail here.
     *
     * Student must never receive tutor contact details
     * through this API.
     */

    private MatchRequestStatus status;

    private String message;

    private LocalDateTime requestedAt;
    private LocalDateTime connectedAt;
    private LocalDateTime finalizedAt;

    private List<MatchRequestSubjectResponse> subjects;
}