package com.mediator.matching.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

import com.mediator.common.dto.PreferredModeDto;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TutorSearchRequest {

    /*
     * Subjects selected by the student.
     *
     * Example:
     * [1, 2, 3]
     */
    @NotEmpty(message = "Please select at least one subject.")
    private List<Long> subjectIds;

    /*
     * Optional board filter.
     */
    private Long boardId;

    /*
     * Optional class-level filter.
     */
    private Long classLevelId;

    /*
     * Requested teaching mode.
     *
     * Example:
     *
     * {
     * "online": true,
     * "studentHome": false,
     * "tutorHome": false
     * }
     */
    private PreferredModeDto preferredMode;

    /*
     * ------------------------------------------------------------
     * STUDENT LOCATION
     * ------------------------------------------------------------
     *
     * Required only when studentHome = true.
     */
    private Double latitude;

    private Double longitude;

    /*
     * ------------------------------------------------------------
     * SEARCH RADIUS
     * ------------------------------------------------------------
     *
     * Unit: KM
     *
     * Used only for studentHome searches.
     */
    @Builder.Default
    @DecimalMin(value = "0.1", message = "Radius must be greater than 0.")
    private Double radius = 10.0;

    /*
     * ------------------------------------------------------------
     * PAGINATION
     * ------------------------------------------------------------
     */

    @Builder.Default
    @Min(value = 0, message = "Page must be 0 or greater.")
    private Integer page = 0;

    @Builder.Default
    @Min(value = 1, message = "Page size must be at least 1.")
    @Max(value = 100, message = "Page size cannot exceed 100.")
    private Integer size = 10;
}