package com.mediator.matching.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TutorTeachingSubjectResponse {

    private Long subjectId;

    private String subjectName;

    private Long classLevelId;

    private String classLevelName;

    private Long boardId;

    private String boardName;
}