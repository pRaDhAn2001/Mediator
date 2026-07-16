package com.mediator.tutor.dto.request;

import com.mediator.tutor.dto.preference.TutorTeachingPreferenceDto;

import jakarta.validation.constraints.NotEmpty;

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
public class TutorTeachingPreferenceRequest {

    @NotEmpty
    private List<TutorTeachingPreferenceDto> preferences;

}
