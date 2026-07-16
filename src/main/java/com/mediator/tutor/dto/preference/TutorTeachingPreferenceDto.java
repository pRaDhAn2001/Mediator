package com.mediator.tutor.dto.preference;

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
public class TutorTeachingPreferenceDto {

    private Long subjectId;
    private Long classLevelId;
    private Long boardId;
}
