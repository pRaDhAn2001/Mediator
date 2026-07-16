package com.mediator.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreferredModeDto {

    @Builder.Default
    private boolean online = false;

    @Builder.Default
    private boolean tutorHome = false;

    @Builder.Default
    private boolean studentHome = false;
}