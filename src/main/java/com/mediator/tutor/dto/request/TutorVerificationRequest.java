package com.mediator.tutor.dto.request;

import java.util.List;

import com.mediator.tutor.dto.document.TutorDocumentDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TutorVerificationRequest {

    private List<TutorDocumentDto> documents;
}