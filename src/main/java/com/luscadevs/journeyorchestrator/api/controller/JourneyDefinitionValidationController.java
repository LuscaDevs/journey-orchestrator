package com.luscadevs.journeyorchestrator.api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.luscadevs.journey.api.generated.model.CreateJourneyDefinitionRequest;
import com.luscadevs.journeyorchestrator.api.dto.ValidationResponse;
import com.luscadevs.journeyorchestrator.application.service.JourneyDefinitionValidationService;
import com.luscadevs.journeyorchestrator.domain.validation.ValidationError;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * Controller para validação de JourneyDefinition sem persistência.
 * 
 * Este controller é responsável apenas por: - Receber requisições HTTP - Delegar lógica de negócio
 * para camada de serviço - Retornar respostas HTTP adequadas
 */
@RestController
@RequestMapping("/journey-definitions")
@RequiredArgsConstructor
@Tag(name = "Journey Definition Validation", description = "Validação de definições de jornada")
public class JourneyDefinitionValidationController {

    private final JourneyDefinitionValidationService validationService;

    /**
     * Valida uma JourneyDefinition sem persistir no banco.
     * 
     * @param request payload da definição a ser validada
     * @return resultado da validação com status HTTP adequado
     */
    @PostMapping("/validate")
    @Operation(summary = "Validar JourneyDefinition",
            description = "Valida uma definição de jornada sem persistir no banco de dados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Validação concluída com sucesso"),
            @ApiResponse(responseCode = "400",
                    description = "Erro de validação ou payload inválido"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")})
    public ResponseEntity<ValidationResponse> validateJourneyDefinition(
            @RequestBody CreateJourneyDefinitionRequest request) {

        try {
            ValidationResponse response = validationService.validateJourneyDefinition(request);

            if (response.isValid()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            // Erros inesperados devem ser logados e retornar 500
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ValidationResponse.failure(List.of(ValidationError
                            .ofInvalidBasicField("server", "Internal server error"))));
        }
    }
}
