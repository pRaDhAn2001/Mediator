package com.mediator.matching.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Subject, class level and board taught by a tutor")
public class TutorTeachingSubjectResponse {

    @Schema(description = "Subject ID", example = "1")
    private Long subjectId;

    @Schema(description = "Subject name", example = "Mathematics")
    private String subjectName;

    @Schema(description = "Class level ID", example = "8")
    private Long classLevelId;

    @Schema(description = "Class level name", example = "Class 8")
    private String classLevelName;

    @Schema(description = "Board ID", example = "1")
    private Long boardId;

    @Schema(description = "Board name", example = "CBSE")
    private String boardName;
}