package com.luscadevs.journeyorchestrator.api.dto;

import java.util.List;

import com.luscadevs.journeyorchestrator.domain.validation.ValidationError;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO para validação de JourneyDefinition.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationResponse {

    /**
     * Indica se a definição é válida.
     */
    private boolean valid;

    /**
     * Lista de erros de validação (se houver).
     */
    private List<ValidationError> errors;

    /**
     * Cria resposta de sucesso.
     */
    public static ValidationResponse success() {
        return ValidationResponse.builder()
                .valid(true)
                .errors(List.of())
                .build();
    }

    /**
     * Cria resposta de erro.
     */
    public static ValidationResponse failure(List<ValidationError> errors) {
        return ValidationResponse.builder()
                .valid(false)
                .errors(errors)
                .build();
    }
}
