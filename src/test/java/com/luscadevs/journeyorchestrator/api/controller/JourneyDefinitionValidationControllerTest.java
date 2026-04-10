package com.luscadevs.journeyorchestrator.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luscadevs.journey.api.generated.model.CreateJourneyDefinitionRequest;
import com.luscadevs.journey.api.generated.model.State;
import com.luscadevs.journey.api.generated.model.StateType;
import com.luscadevs.journey.api.generated.model.TransitionRequest;
import com.luscadevs.journeyorchestrator.api.dto.ValidationResponse;
import com.luscadevs.journeyorchestrator.application.service.JourneyDefinitionValidationService;
import com.luscadevs.journeyorchestrator.config.TestConfig;
import com.luscadevs.journeyorchestrator.domain.validation.ValidationError;

/**
 * Testes de integração para JourneyDefinitionValidationController.
 * 
 * Segue os melhores padrões do Spring Boot Test: - @WebMvcTest para testar apenas a camada web
 * - @MockBean para mockar dependências - MockMvc para testes HTTP reais - Testes focados em
 * contratos HTTP
 */
@WebMvcTest(JourneyDefinitionValidationController.class)
@Import(TestConfig.class)
class JourneyDefinitionValidationControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private JourneyDefinitionValidationService validationService;

        @Autowired
        private ObjectMapper objectMapper;

        private CreateJourneyDefinitionRequest validRequest;
        private CreateJourneyDefinitionRequest invalidRequest;

        @BeforeEach
        void setUp() {
                // Request válido com estrutura completa
                validRequest = new CreateJourneyDefinitionRequest().journeyCode("TEST_JOURNEY")
                                .name("Test Journey").version(1)
                                .states(List.of(new State().name("START").type(StateType.INITIAL),
                                                new State().name("END").type(StateType.FINAL)))
                                .transitions(List.of(new TransitionRequest().source("START")
                                                .target("END").event("PROCEED")));

                // Request inválido com campos básicos inválidos
                invalidRequest = new CreateJourneyDefinitionRequest().journeyCode("") // Código
                                                                                      // vazio
                                .name("") // Nome vazio
                                .version(0) // Versão inválida
                                .states(List.of(new State().name("START").type(StateType.INITIAL),
                                                new State().name("END").type(StateType.FINAL)))
                                .transitions(List.of(new TransitionRequest().source("START")
                                                .target("END").event("PROCEED")));
        }

        @Test
        @DisplayName("Deve retornar 200 com valid=true para definição válida")
        void shouldReturn200WithValidTrueForValidDefinition() throws Exception {
                // Arrange
                when(validationService.validateJourneyDefinition(any()))
                                .thenReturn(ValidationResponse.success());

                // Act & Assert
                mockMvc.perform(post("/journey-definitions/validate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validRequest)))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.valid").value(true))
                                .andExpect(jsonPath("$.errors").isEmpty());

                verify(validationService).validateJourneyDefinition(any());
        }

        @Test
        @DisplayName("Deve retornar 400 com valid=false e erros estruturados para definição inválida")
        void shouldReturn400WithValidFalseAndStructuredErrorsForInvalidDefinition()
                        throws Exception {
                // Arrange
                ValidationError error1 = ValidationError.ofInvalidBasicField("journeyCode",
                                "Journey code is required");
                ValidationError error2 = ValidationError.ofInvalidBasicField("name",
                                "Journey name is required");
                ValidationError error3 = ValidationError.ofInvalidBasicField("version",
                                "Journey version must be a positive integer");

                when(validationService.validateJourneyDefinition(any())).thenReturn(
                                ValidationResponse.failure(List.of(error1, error2, error3)));

                // Act & Assert
                mockMvc.perform(post("/journey-definitions/validate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidRequest)))
                                .andExpect(status().isBadRequest())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.valid").value(false))
                                .andExpect(jsonPath("$.errors").isArray())
                                .andExpect(jsonPath("$.errors.length()").value(3))
                                .andExpect(jsonPath("$.errors[0].code")
                                                .value("INVALID_BASIC_FIELDS"))
                                .andExpect(jsonPath("$.errors[0].message")
                                                .value("Journey code is required"))
                                .andExpect(jsonPath("$.errors[0].field").value("journeyCode"))
                                .andExpect(jsonPath("$.errors[1].code")
                                                .value("INVALID_BASIC_FIELDS"))
                                .andExpect(jsonPath("$.errors[1].message")
                                                .value("Journey name is required"))
                                .andExpect(jsonPath("$.errors[1].field").value("name"))
                                .andExpect(jsonPath("$.errors[2].code")
                                                .value("INVALID_BASIC_FIELDS"))
                                .andExpect(jsonPath("$.errors[2].message").value(
                                                "Journey version must be a positive integer"))
                                .andExpect(jsonPath("$.errors[2].field").value("version"));

                verify(validationService).validateJourneyDefinition(any());
        }

        @Test
        @DisplayName("Deve retornar 400 com erro de transição ambígua")
        void shouldReturn400WithAmbiguousTransitionError() throws Exception {
                // Arrange
                ValidationError error = ValidationError.ofAmbiguousTransition("START", "EVALUATE");

                when(validationService.validateJourneyDefinition(any()))
                                .thenReturn(ValidationResponse.failure(List.of(error)));

                // Act & Assert
                mockMvc.perform(post("/journey-definitions/validate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validRequest)))
                                .andExpect(status().isBadRequest())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.valid").value(false))
                                .andExpect(jsonPath("$.errors").isArray())
                                .andExpect(jsonPath("$.errors.length()").value(1))
                                .andExpect(jsonPath("$.errors[0].code")
                                                .value("AMBIGUOUS_TRANSITION"))
                                .andExpect(jsonPath("$.errors[0].message").value(
                                                "Ambiguous transition: state 'START' with event 'EVALUATE' has multiple paths without clear conditions"))
                                .andExpect(jsonPath("$.errors[0].field").value("transitions"));

                verify(validationService).validateJourneyDefinition(any());
        }

        @Test
        @DisplayName("Deve retornar 500 para erro inesperado no serviço")
        void shouldReturn500ForUnexpectedError() throws Exception {
                // Arrange
                when(validationService.validateJourneyDefinition(any()))
                                .thenThrow(new RuntimeException("Unexpected error"));

                // Act & Assert
                mockMvc.perform(post("/journey-definitions/validate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validRequest)))
                                .andExpect(status().isInternalServerError())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.valid").value(false))
                                .andExpect(jsonPath("$.errors").isArray())
                                .andExpect(jsonPath("$.errors.length()").value(1))
                                .andExpect(jsonPath("$.errors[0].code")
                                                .value("INVALID_BASIC_FIELDS"))
                                .andExpect(jsonPath("$.errors[0].field").value("server"));

                verify(validationService).validateJourneyDefinition(any());
        }

        @Test
        @DisplayName("Deve retornar 500 para payload JSON inválido")
        void shouldReturn500ForInvalidJsonPayload() throws Exception {
                // Act & Assert
                mockMvc.perform(post("/journey-definitions/validate")
                                .contentType(MediaType.APPLICATION_JSON).content("{invalid json}"))
                                .andExpect(status().isInternalServerError());
        }

        @Test
        @DisplayName("Deve manter resposta consistente com múltiplos erros")
        void shouldMaintainConsistentResponseWithMultipleErrors() throws Exception {
                // Arrange
                ValidationError error1 = ValidationError.ofDuplicateState("DUPLICATE");
                ValidationError error2 = ValidationError.ofMissingInitialState();
                ValidationError error3 = ValidationError.ofUnreachableState("ORPHAN");

                when(validationService.validateJourneyDefinition(any())).thenReturn(
                                ValidationResponse.failure(List.of(error1, error2, error3)));

                // Act & Assert
                mockMvc.perform(post("/journey-definitions/validate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validRequest)))
                                .andExpect(status().isBadRequest())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.valid").value(false))
                                .andExpect(jsonPath("$.errors").isArray())
                                .andExpect(jsonPath("$.errors.length()").value(3))
                                .andExpect(jsonPath("$.errors[0].code")
                                                .value("DUPLICATE_STATE_NAME"))
                                .andExpect(jsonPath("$.errors[1].code")
                                                .value("MISSING_INITIAL_STATE"))
                                .andExpect(jsonPath("$.errors[2].code").value("UNREACHABLE_STATE"));

                verify(validationService).validateJourneyDefinition(any());
        }

        @Test
        @DisplayName("Deve retornar 400 para erro de estado duplicado")
        void shouldReturn400ForDuplicateStateError() throws Exception {
                // Arrange
                ValidationError error = ValidationError.ofDuplicateState("DUPLICATE_STATE");

                when(validationService.validateJourneyDefinition(any()))
                                .thenReturn(ValidationResponse.failure(List.of(error)));

                // Act & Assert
                mockMvc.perform(post("/journey-definitions/validate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validRequest)))
                                .andExpect(status().isBadRequest())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.valid").value(false))
                                .andExpect(jsonPath("$.errors").isArray())
                                .andExpect(jsonPath("$.errors.length()").value(1))
                                .andExpect(jsonPath("$.errors[0].code")
                                                .value("DUPLICATE_STATE_NAME"))
                                .andExpect(jsonPath("$.errors[0].message")
                                                .value("Duplicate state name: DUPLICATE_STATE"))
                                .andExpect(jsonPath("$.errors[0].field").value("states"));

                verify(validationService).validateJourneyDefinition(any());
        }
}
