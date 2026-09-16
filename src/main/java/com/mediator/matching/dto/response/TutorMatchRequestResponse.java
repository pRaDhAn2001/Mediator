package com.mediator.matching.dto.response;

import com.mediator.matching.entity.MatchRequestStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TutorMatchRequestResponse {

    private Long requestId;

    private Long studentId;

    private String studentName;

    private String studentPhone;

    private String studentEmail;

    private Long tutorId;

    private String tutorName;

    private MatchRequestStatus status;

    private String message;

    private LocalDateTime requestedAt;

    private LocalDateTime connectedAt;

    private LocalDateTime finalizedAt;

    private List<MatchRequestSubjectResponse> subjects;
}
