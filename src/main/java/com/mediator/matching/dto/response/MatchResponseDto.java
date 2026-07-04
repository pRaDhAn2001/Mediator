package com.mediator.matching.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.mediator.matching.entity.MatchRequestStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchResponseDto {

    private Long requestId;
    private Long tutorId;
    private String tutorName;
    private List<String> subjects;
    private MatchRequestStatus status;
    private LocalDateTime createdAt;
}
