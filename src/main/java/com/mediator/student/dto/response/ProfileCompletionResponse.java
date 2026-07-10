package com.mediator.student.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileCompletionResponse {

    private Integer completionPercentage;

    private String nextAction;
}