package com.mediator.student.dto.dashboard;

import com.mediator.student.dto.response.StudentProfileResponse;

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
@Schema(description = "Summary information displayed on the student dashboard.")
public class StudentDashboardResponse {

    @Schema(description = "Student profile summary.")
    private StudentProfileResponse profile;

    @Schema(description = "Profile completion percentage.", example = "85", minimum = "0", maximum = "100")
    private Integer profileCompletion;

    @Schema(description = "Number of active matches.", example = "2")
    private Long activeMatchesCount;

    @Schema(description = "Number of pending match requests.", example = "3")
    private Long pendingMatchesCount;

    @Schema(description = "Number of cancelled matches.", example = "1")
    private Long cancelledMatchesCount;
}
