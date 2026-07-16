package com.mediator.tutor.dto.document;

import com.mediator.tutor.entity.DocumentType;
import com.mediator.tutor.entity.VerificationStatus;

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
public class TutorDocumentDto {

    private Long documentId;

    private DocumentType documentType;

    private String documentUrl;

    private VerificationStatus verificationStatus;

    private String remarks;

}
