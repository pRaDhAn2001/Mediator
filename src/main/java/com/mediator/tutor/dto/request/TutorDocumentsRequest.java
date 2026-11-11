package com.mediator.tutor.dto.request;

import java.util.List;

import com.mediator.tutor.dto.document.TutorDocumentDto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
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
@Schema(description = "Request containing the documents submitted by a tutor.")
public class TutorDocumentsRequest {

    @Valid
    @NotEmpty(message = "At least one document is required.")
    @ArraySchema(arraySchema = @Schema(description = "List of tutor documents. At least one document is required."), schema = @Schema(implementation = TutorDocumentDto.class))
    private List<TutorDocumentDto> documents;
}