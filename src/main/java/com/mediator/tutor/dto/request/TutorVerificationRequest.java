package com.mediator.tutor.dto.request;

import java.util.List;

import com.mediator.tutor.dto.document.TutorDocumentDto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request containing documents submitted for tutor verification.")
public class TutorVerificationRequest {

    @ArraySchema(arraySchema = @Schema(description = "Documents submitted for verification."), schema = @Schema(implementation = TutorDocumentDto.class))
    private List<TutorDocumentDto> documents;
}