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
@Schema(description = "Educational board information")
public class BoardResponse {

    @Schema(description = "Unique board ID", example = "1")
    private Long id;

    @Schema(description = "Name of the educational board", example = "CBSE")
    private String name;
}