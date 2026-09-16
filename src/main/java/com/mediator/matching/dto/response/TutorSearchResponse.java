package com.mediator.matching.dto.response;

import java.util.List;

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
public class TutorSearchResponse {

    private List<TutorCardResponse> tutors;

    private Integer currentPage;

    private Integer totalPages;

    private Long totalElements;

    private Boolean hasNext;
}