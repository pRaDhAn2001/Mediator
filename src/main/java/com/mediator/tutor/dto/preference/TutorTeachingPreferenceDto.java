package com.mediator.tutor.dto.preference;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "A single tutor teaching preference.")
public class TutorTeachingPreferenceDto {

    @Schema(description = "ID of the subject the tutor wants to teach.", example = "1")
    private Long subjectId;

    @Schema(description = "ID of the class level the tutor wants to teach.", example = "3")
    private Long classLevelId;

    @Schema(description = "ID of the education board the tutor supports.", example = "1")
    private Long boardId;
}