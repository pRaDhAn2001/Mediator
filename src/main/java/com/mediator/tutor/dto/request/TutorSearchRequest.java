package com.mediator.tutor.dto.request;

import java.util.List;

import com.mediator.common.base.PreferredMode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TutorSearchRequest {

    private List<Long> subjectIds;
    private Long classLevelId;
    private Long boardId;
    private PreferredMode preferredMode;
    private Double radius;
    private Double maxSalary;
    private Boolean verifiedOnly;
    private Integer page;
    private Integer size;
}
