package com.luscadevs.journeyorchestrator.application.service;

import com.luscadevs.journey.api.generated.model.CreateJourneyDefinitionRequest;
import com.luscadevs.journeyorchestrator.api.mapper.JourneyDefinitionMapper;
import com.luscadevs.journeyorchestrator.application.port.out.JourneyDefinitionRepositoryPort;
import com.luscadevs.journeyorchestrator.domain.exception.JourneyDefinitionNotFoundException;
import com.luscadevs.journeyorchestrator.domain.exception.JourneyDefinitionAlreadyExistsException;
import com.luscadevs.journeyorchestrator.domain.exception.ErrorCode;
import com.luscadevs.journeyorchestrator.domain.journey.JourneyDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.mockito.MockedStatic;

/**
 * Integration tests to verify that domain exceptions are properly used
 * in JourneyDefinitionService methods.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JourneyDefinitionServiceIntegrationTest {

        @Mock
        private JourneyDefinitionRepositoryPort repository;

        private JourneyDefinitionService journeyDefinitionService;

        @BeforeEach
        void setUp() {
                journeyDefinitionService = new JourneyDefinitionService(repository);
        }

        @Test
        void shouldThrowJourneyDefinitionAlreadyExistsExceptionWhenCreatingDuplicateDefinition() {
                // Given
                String journeyCode = "TEST_JOURNEY";
                Integer version = 1;
                CreateJourneyDefinitionRequest request = new CreateJourneyDefinitionRequest()
                                .journeyCode(journeyCode)
                                .version(version);

                JourneyDefinition existingDefinition = mock(JourneyDefinition.class);
                when(existingDefinition.getJourneyCode()).thenReturn(journeyCode);
                when(existingDefinition.getVersion()).thenReturn(version);

                // Mock the mapper to return a JourneyDefinition
                try (MockedStatic<JourneyDefinitionMapper> mapperMock = mockStatic(JourneyDefinitionMapper.class)) {
                        JourneyDefinition newDefinition = mock(JourneyDefinition.class);
                        when(newDefinition.getJourneyCode()).thenReturn(journeyCode);
                        when(newDefinition.getVersion()).thenReturn(version);

                        mapperMock.when(() -> JourneyDefinitionMapper.toDomain(request))
                                        .thenReturn(newDefinition);

                        when(repository.findByJourneyCodeAndVersion(journeyCode, version))
                                        .thenReturn(Optional.of(existingDefinition));

                        // When & Then
                        JourneyDefinitionAlreadyExistsException exception = assertThrows(
                                        JourneyDefinitionAlreadyExistsException.class,
                                        () -> journeyDefinitionService.createJourneyDefinition(request));

                        assertEquals(journeyCode + ":" + version, exception.getJourneyDefinitionId());
                        assertEquals(ErrorCode.JOURNEY_DEFINITION_ALREADY_EXISTS, exception.getErrorCode());
                        verify(repository).findByJourneyCodeAndVersion(journeyCode, version);
                        verify(repository, never()).save(any());
                }
        }

        @Test
        void shouldCreateJourneyDefinitionWhenNoDuplicateExists() {
                // Given
                String journeyCode = "NEW_JOURNEY";
                Integer version = 1;
                CreateJourneyDefinitionRequest request = new CreateJourneyDefinitionRequest()
                                .journeyCode(journeyCode)
                                .version(version);

                // Mock the mapper to return a JourneyDefinition
                try (MockedStatic<JourneyDefinitionMapper> mapperMock = mockStatic(JourneyDefinitionMapper.class)) {
                        JourneyDefinition newDefinition = mock(JourneyDefinition.class);
                        when(newDefinition.getJourneyCode()).thenReturn(journeyCode);
                        when(newDefinition.getVersion()).thenReturn(version);

                        mapperMock.when(() -> JourneyDefinitionMapper.toDomain(request))
                                        .thenReturn(newDefinition);

                        when(repository.findByJourneyCodeAndVersion(journeyCode, version))
                                        .thenReturn(Optional.empty());

                        JourneyDefinition savedDefinition = mock(JourneyDefinition.class);
                        when(repository.save(any(JourneyDefinition.class)))
                                        .thenReturn(savedDefinition);

                        // When
                        JourneyDefinition result = journeyDefinitionService.createJourneyDefinition(request);

                        // Then
                        assertNotNull(result);
                        verify(repository).findByJourneyCodeAndVersion(journeyCode, version);
                        verify(repository).save(any(JourneyDefinition.class));
                }
        }

        @Test
        void shouldThrowJourneyDefinitionNotFoundExceptionWhenGettingDefinitionsByNonExistentCode() {
                // Given
                String nonExistentCode = "NON_EXISTENT";

                when(repository.findByCode(nonExistentCode))
                                .thenReturn(Optional.empty());

                // When & Then
                JourneyDefinitionNotFoundException exception = assertThrows(
                                JourneyDefinitionNotFoundException.class,
                                () -> journeyDefinitionService.getJourneyDefinitionsByCode(nonExistentCode));

                assertEquals(nonExistentCode, exception.getJourneyDefinitionId());
                verify(repository).findByCode(nonExistentCode);
        }

        @Test
        void shouldReturnJourneyDefinitionsWhenCodeExists() {
                // Given
                String existingCode = "EXISTING_CODE";
                List<JourneyDefinition> definitions = List.of(mock(JourneyDefinition.class));

                when(repository.findByCode(existingCode))
                                .thenReturn(Optional.of(definitions));

                // When
                List<JourneyDefinition> result = journeyDefinitionService.getJourneyDefinitionsByCode(existingCode);

                // Then
                assertNotNull(result);
                assertEquals(definitions, result);
                verify(repository).findByCode(existingCode);
        }

        @Test
        void shouldThrowJourneyDefinitionNotFoundExceptionWhenGettingNonExistentDefinition() {
                // Given
                String journeyCode = "NON_EXISTENT";
                Integer version = 1;

                when(repository.findByJourneyCodeAndVersion(journeyCode, version))
                                .thenReturn(Optional.empty());

                // When & Then
                JourneyDefinitionNotFoundException exception = assertThrows(
                                JourneyDefinitionNotFoundException.class,
                                () -> journeyDefinitionService.getJourneyDefinition(journeyCode, version));

                assertEquals(journeyCode + ":" + version, exception.getJourneyDefinitionId());
                verify(repository).findByJourneyCodeAndVersion(journeyCode, version);
        }

        @Test
        void shouldReturnJourneyDefinitionWhenExists() {
                // Given
                String journeyCode = "EXISTING";
                Integer version = 1;
                JourneyDefinition definition = mock(JourneyDefinition.class);

                when(repository.findByJourneyCodeAndVersion(journeyCode, version))
                                .thenReturn(Optional.of(definition));

                // When
                JourneyDefinition result = journeyDefinitionService.getJourneyDefinition(journeyCode, version);

                // Then
                assertNotNull(result);
                assertEquals(definition, result);
                verify(repository).findByJourneyCodeAndVersion(journeyCode, version);
        }

        @Test
        void shouldReturnAllJourneyDefinitions() {
                // Given
                List<JourneyDefinition> allDefinitions = List.of(
                                mock(JourneyDefinition.class),
                                mock(JourneyDefinition.class));

                when(repository.findAll())
                                .thenReturn(allDefinitions);

                // When
                List<JourneyDefinition> result = journeyDefinitionService.getAllJourneyDefinitions();

                // Then
                assertNotNull(result);
                assertEquals(allDefinitions, result);
                verify(repository).findAll();
        }

        @Test
        void shouldReturnEmptyListWhenNoJourneyDefinitionsExist() {
                // Given
                List<JourneyDefinition> emptyList = List.of();

                when(repository.findAll())
                                .thenReturn(emptyList);

                // When
                List<JourneyDefinition> result = journeyDefinitionService.getAllJourneyDefinitions();

                // Then
                assertNotNull(result);
                assertTrue(result.isEmpty());
                verify(repository).findAll();
        }
}
