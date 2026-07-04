package com.mediator.student.dto.dashboard;

import com.mediator.student.dto.response.StudentProfileResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentDashboardResponse {
    private StudentProfileResponse profile;
    private Long activeMatchesCount;
    private Long pendingMatchesCount;
    private Long cancelledMatchesCount;
}
