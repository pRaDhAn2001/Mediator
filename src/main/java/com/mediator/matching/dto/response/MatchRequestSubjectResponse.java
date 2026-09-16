package com.mediator.matching.dto.response;

import com.mediator.matching.entity.SubjectRequestStatus;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchRequestSubjectResponse {

    private Long subjectId;

    private String subjectName;

    private SubjectRequestStatus status;
}