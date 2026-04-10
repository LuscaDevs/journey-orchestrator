package com.luscadevs.journeyorchestrator.domain.exception;

import java.util.ArrayList;
import java.util.List;

import com.luscadevs.journeyorchestrator.domain.validation.ValidationError;

/**
 * Exceção lançada quando uma JourneyDefinition contém erros de validação estrutural. Contém uma
 * lista de erros estruturados para facilitar o diagnóstico e tratamento pelo frontend.
 */
public class JourneyDefinitionValidationException extends RuntimeException {

    private final List<ValidationError> errors;

    public JourneyDefinitionValidationException(List<ValidationError> errors) {
        super("Journey definition validation failed: " + errors.stream()
                .map(ValidationError::getMessage).reduce((a, b) -> a + "; " + b).orElse(""));
        this.errors = new ArrayList<>(errors);
    }

    public JourneyDefinitionValidationException(ValidationError error) {
        this(List.of(error));
    }

    public JourneyDefinitionValidationException(String error) {
        this(List.of(ValidationError.builder().code(ValidationError.Codes.INVALID_BASIC_FIELDS)
                .message(error).build()));
    }

    public List<ValidationError> getErrors() {
        return new ArrayList<>(errors);
    }

    /**
     * Verifica se há erros de um tipo específico.
     * 
     * @param errorCode código do erro para filtrar
     * @return true se houver erros com o código especificado
     */
    public boolean hasErrorsWithCode(String errorCode) {
        return errors.stream().anyMatch(error -> errorCode.equals(error.getCode()));
    }

    /**
     * Verifica se há erros de um tipo específico (mantido para compatibilidade).
     * 
     * @param keyword palavra-chave para filtrar erros
     * @return true se houver erros contendo a palavra-chave
     */
    public boolean hasErrorsContaining(String keyword) {
        return errors.stream()
                .anyMatch(error -> error.getMessage().toLowerCase().contains(keyword.toLowerCase())
                        || (error.getField() != null
                                && error.getField().toLowerCase().contains(keyword.toLowerCase())));
    }

    /**
     * Retorna os erros formatados para exibição.
     * 
     * @return string com erros numerados
     */
    public String getFormattedErrors() {
        StringBuilder sb = new StringBuilder();
        sb.append("Journey definition validation errors:\n");
        for (int i = 0; i < errors.size(); i++) {
            ValidationError error = errors.get(i);
            sb.append(i + 1).append(". [").append(error.getCode()).append("] ");
            if (error.getField() != null) {
                sb.append("Field '").append(error.getField()).append("': ");
            }
            sb.append(error.getMessage()).append("\n");
        }
        return sb.toString();
    }

    /**
     * Retorna erros como lista de strings (mantido para compatibilidade).
     * 
     * @return lista de mensagens de erro
     */
    public List<String> getErrorMessages() {
        return errors.stream().map(ValidationError::getMessage).toList();
    }
}
