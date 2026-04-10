package com.luscadevs.journeyorchestrator.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.luscadevs.journey.api.generated.model.CreateJourneyDefinitionRequest;
import com.luscadevs.journeyorchestrator.api.dto.ValidationResponse;
import com.luscadevs.journeyorchestrator.api.mapper.JourneyDefinitionMapper;
import com.luscadevs.journeyorchestrator.domain.exception.JourneyDefinitionValidationException;
import com.luscadevs.journeyorchestrator.domain.journey.JourneyDefinition;
import com.luscadevs.journeyorchestrator.domain.validation.JourneyDefinitionValidator;
import com.luscadevs.journeyorchestrator.domain.validation.ValidationError;

import lombok.RequiredArgsConstructor;

/**
 * Serviço de validação de JourneyDefinition.
 * 
 * Este serviço é responsável por:
 * - Converter request API para domínio
 * - Orquestrar validação estrutural
 * - Tratar exceções de forma centralizada
 * - Retornar resposta padronizada
 */
@Service
@RequiredArgsConstructor
public class JourneyDefinitionValidationService {

    private final JourneyDefinitionValidator validator;

    /**
     * Valida uma JourneyDefinition sem persistir no banco.
     * 
     * @param request payload da definição a ser validada
     * @return resultado da validação
     */
    public ValidationResponse validateJourneyDefinition(CreateJourneyDefinitionRequest request) {
        try {
            // 1. Converter request para domínio
            JourneyDefinition definition = convertToDomain(request);
            
            // 2. Validar estrutura
            validator.validate(definition);
            
            // 3. Retornar sucesso
            return ValidationResponse.success();
            
        } catch (JourneyDefinitionValidationException e) {
            // Erros de validação estrutural
            return ValidationResponse.failure(e.getErrors());
            
        } catch (IllegalArgumentException e) {
            // Erros de mapeamento/conversão
            return ValidationResponse.failure(List.of(
                ValidationError.ofInvalidBasicField("request", e.getMessage())
            ));
            
        } catch (Exception e) {
            // Erros inesperados (deveriam ser logados)
            return ValidationResponse.failure(List.of(
                ValidationError.ofInvalidBasicField("server", "Validation service error: " + e.getMessage())
            ));
        }
    }

    /**
     * Converte request API para domínio de forma segura.
     * 
     * @param request request da API
     * @return JourneyDefinition no formato de domínio
     * @throws IllegalArgumentException se houver erro na conversão
     */
    private JourneyDefinition convertToDomain(CreateJourneyDefinitionRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        
        try {
            return JourneyDefinitionMapper.toDomain(request);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to convert request to domain: " + e.getMessage(), e);
        }
    }
}
