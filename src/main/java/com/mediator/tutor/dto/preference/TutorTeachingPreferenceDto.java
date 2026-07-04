package com.mediator.tutor.dto.preference;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TutorTeachingPreferenceDto {

    private Long subjectId;
    private Long classLevelId;
    private Long boardId;
}
