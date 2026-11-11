package com.mediator.tutor.dto.document;

import com.mediator.tutor.entity.DocumentType;
import com.mediator.tutor.entity.VerificationStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@Schema(description = "Tutor document information and its verification details.")
public class TutorDocumentDto {

    @Schema(description = "Unique document ID. Usually populated in document responses.", example = "12", accessMode = Schema.AccessMode.READ_ONLY)
    private Long documentId;

    @NotNull(message = "Document type is required.")
    @Schema(description = "Type of document being submitted.", example = "CLASS_10_MARKSHEET", requiredMode = Schema.RequiredMode.REQUIRED)
    private DocumentType documentType;

    @NotBlank(message = "Document URL is required.")
    @Schema(description = "URL pointing to the uploaded document.", example = "https://example.com/documents/class10.pdf", requiredMode = Schema.RequiredMode.REQUIRED)
    private String documentUrl;

    @Schema(description = "Verification status of the submitted document.", example = "PENDING", accessMode = Schema.AccessMode.READ_ONLY)
    private VerificationStatus verificationStatus;

    @Schema(description = "Remarks added during document verification.", example = "Document is clear and readable.", accessMode = Schema.AccessMode.READ_ONLY)
    private String remarks;
}