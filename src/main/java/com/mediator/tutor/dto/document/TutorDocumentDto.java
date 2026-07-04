package com.mediator.tutor.dto.document;

import com.mediator.tutor.entity.DocumentType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TutorDocumentDto {

    private DocumentType documentType;
    private String documentUrl;
}
