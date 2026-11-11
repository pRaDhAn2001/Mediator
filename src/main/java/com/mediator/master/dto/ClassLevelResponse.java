package com.mediator.master.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Academic class level information")
public class ClassLevelResponse {

    @Schema(description = "Unique class level ID", example = "8")
    private Long id;

    @Schema(description = "Standard or class number", example = "8")
    private Integer standard;
}