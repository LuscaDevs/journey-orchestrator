package com.luscadevs.journeyorchestrator.unit.mongo.repository;

import com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.document.JourneyDefinitionDocument;
import com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.mapper.JourneyDefinitionDocumentMapper;
import com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.repository.JourneyDefinitionRepositoryImpl;
import com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.repository.MongoJourneyDefinitionRepository;
import com.luscadevs.journeyorchestrator.domain.journey.JourneyDefinition;
import com.luscadevs.journeyorchestrator.domain.journey.State;
import com.luscadevs.journeyorchestrator.domain.journey.Transition;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for JourneyDefinitionRepositoryImpl.
 * Tests repository operations and mapping integration.
 */
@ExtendWith(MockitoExtension.class)
class JourneyDefinitionRepositoryImplTest {

    @Mock
    private MongoJourneyDefinitionRepository mongoJourneyDefinitionRepository;

    @Mock
    private JourneyDefinitionDocumentMapper mapper;

    private JourneyDefinitionRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new JourneyDefinitionRepositoryImpl(mongoJourneyDefinitionRepository, mapper);
    }

    @Test
    @DisplayName("Should save journey definition")
    void shouldSaveJourneyDefinition() {
        // Given
        JourneyDefinition domain = createTestJourneyDefinition();
        JourneyDefinitionDocument document = createTestJourneyDefinitionDocument();
        JourneyDefinitionDocument savedDocument = createTestJourneyDefinitionDocument();
        JourneyDefinition expectedDomain = createTestJourneyDefinition();

        when(mapper.toDocument(domain)).thenReturn(document);
        when(mongoJourneyDefinitionRepository.save(document)).thenReturn(savedDocument);
        when(mapper.toDomain(savedDocument)).thenReturn(expectedDomain);

        // When
        JourneyDefinition result = repository.save(domain);

        // Then
        assertThat(result).isEqualTo(expectedDomain);
        verify(mapper).toDocument(domain);
        verify(mongoJourneyDefinitionRepository).save(document);
        verify(mapper).toDomain(savedDocument);
    }

    @Test
    @DisplayName("Should find journey definition by code and version")
    void shouldFindByJourneyCodeAndVersion() {
        // Given
        String journeyCode = "TEST_JOURNEY";
        Integer version = 1;
        JourneyDefinitionDocument document = createTestJourneyDefinitionDocument();
        JourneyDefinition expectedDomain = createTestJourneyDefinition();

        when(mongoJourneyDefinitionRepository.findByJourneyCodeAndVersion(journeyCode, version.toString()))
                .thenReturn(Optional.of(document));
        when(mapper.toDomain(document)).thenReturn(expectedDomain);

        // When
        Optional<JourneyDefinition> result = repository.findByJourneyCodeAndVersion(journeyCode, version);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(expectedDomain);
        verify(mongoJourneyDefinitionRepository).findByJourneyCodeAndVersion(journeyCode, version.toString());
        verify(mapper).toDomain(document);
    }

    @Test
    @DisplayName("Should return empty when journey definition not found by code and version")
    void shouldReturnEmptyWhenNotFoundByCodeAndVersion() {
        // Given
        String journeyCode = "NON_EXISTENT";
        Integer version = 1;

        when(mongoJourneyDefinitionRepository.findByJourneyCodeAndVersion(journeyCode, version.toString()))
                .thenReturn(Optional.empty());

        // When
        Optional<JourneyDefinition> result = repository.findByJourneyCodeAndVersion(journeyCode, version);

        // Then
        assertThat(result).isEmpty();
        verify(mongoJourneyDefinitionRepository).findByJourneyCodeAndVersion(journeyCode, version.toString());
        verify(mapper, never()).toDomain(any());
    }

    @Test
    @DisplayName("Should find journey definitions by code")
    void shouldFindByCode() {
        // Given
        String journeyCode = "TEST_JOURNEY";
        List<JourneyDefinitionDocument> documents = List.of(
                createTestJourneyDefinitionDocument(),
                createTestJourneyDefinitionDocument()
        );
        List<JourneyDefinition> expectedDomains = List.of(
                createTestJourneyDefinition(),
                createTestJourneyDefinition()
        );

        when(mongoJourneyDefinitionRepository.findByJourneyCode(journeyCode)).thenReturn(documents);
        when(mapper.toDomain(any(JourneyDefinitionDocument.class)))
                .thenReturn(expectedDomains.get(0), expectedDomains.get(1));

        // When
        Optional<List<JourneyDefinition>> result = repository.findByCode(journeyCode);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).hasSize(2);
        assertThat(result.get()).isEqualTo(expectedDomains);
        verify(mongoJourneyDefinitionRepository).findByJourneyCode(journeyCode);
        verify(mapper, times(2)).toDomain(any(JourneyDefinitionDocument.class));
    }

    @Test
    @DisplayName("Should return empty when no journey definitions found by code")
    void shouldReturnEmptyWhenNoDefinitionsFoundByCode() {
        // Given
        String journeyCode = "NON_EXISTENT";

        when(mongoJourneyDefinitionRepository.findByJourneyCode(journeyCode)).thenReturn(List.of());

        // When
        Optional<List<JourneyDefinition>> result = repository.findByCode(journeyCode);

        // Then
        assertThat(result).isEmpty();
        verify(mongoJourneyDefinitionRepository).findByJourneyCode(journeyCode);
        verify(mapper, never()).toDomain(any());
    }

    @Test
    @DisplayName("Should find all journey definitions")
    void shouldFindAll() {
        // Given
        List<JourneyDefinitionDocument> documents = List.of(
                createTestJourneyDefinitionDocument(),
                createTestJourneyDefinitionDocument(),
                createTestJourneyDefinitionDocument()
        );
        List<JourneyDefinition> expectedDomains = List.of(
                createTestJourneyDefinition(),
                createTestJourneyDefinition(),
                createTestJourneyDefinition()
        );

        when(mongoJourneyDefinitionRepository.findAll()).thenReturn(documents);
        when(mapper.toDomain(any(JourneyDefinitionDocument.class)))
                .thenReturn(expectedDomains.get(0), expectedDomains.get(1), expectedDomains.get(2));

        // When
        List<JourneyDefinition> result = repository.findAll();

        // Then
        assertThat(result).hasSize(3);
        assertThat(result).isEqualTo(expectedDomains);
        verify(mongoJourneyDefinitionRepository).findAll();
        verify(mapper, times(3)).toDomain(any(JourneyDefinitionDocument.class));
    }

    @Test
    @DisplayName("Should return empty list when no journey definitions exist")
    void shouldReturnEmptyListWhenNoDefinitionsExist() {
        // Given
        when(mongoJourneyDefinitionRepository.findAll()).thenReturn(List.of());

        // When
        List<JourneyDefinition> result = repository.findAll();

        // Then
        assertThat(result).isEmpty();
        verify(mongoJourneyDefinitionRepository).findAll();
        verify(mapper, never()).toDomain(any());
    }

    @Test
    @DisplayName("Should handle null journey definition save")
    void shouldHandleNullJourneyDefinitionSave() {
        // Given
        JourneyDefinition domain = null;

        // When
        JourneyDefinition result = repository.save(domain);

        // Then
        assertThat(result).isNull();
        verify(mapper, never()).toDocument(any());
        verify(mongoJourneyDefinitionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should handle mapping exceptions gracefully")
    void shouldHandleMappingExceptionsGracefully() {
        // Given
        JourneyDefinition domain = createTestJourneyDefinition();

        when(mapper.toDocument(domain)).thenThrow(new RuntimeException("Mapping error"));

        // When/Then
        assertThatThrownBy(() -> repository.save(domain))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Mapping error");
        verify(mapper).toDocument(domain);
        verify(mongoJourneyDefinitionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should handle repository exceptions gracefully")
    void shouldHandleRepositoryExceptionsGracefully() {
        // Given
        JourneyDefinition domain = createTestJourneyDefinition();
        JourneyDefinitionDocument document = createTestJourneyDefinitionDocument();

        when(mapper.toDocument(domain)).thenReturn(document);
        when(mongoJourneyDefinitionRepository.save(document))
                .thenThrow(new RuntimeException("Repository error"));

        // When/Then
        assertThatThrownBy(() -> repository.save(domain))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Repository error");
        verify(mapper).toDocument(domain);
        verify(mongoJourneyDefinitionRepository).save(document);
    }

    private JourneyDefinition createTestJourneyDefinition() {
        State startState = State.builder().name("START").build();
        State endState = State.builder().name("END").build();
        
        Transition transition = Transition.builder()
                .id("transition-1")
                .sourceState(startState)
                .targetState(endState)
                .event(com.luscadevs.journeyorchestrator.domain.journey.Event.of("COMPLETE"))
                .build();

        return JourneyDefinition.builder()
                .id("test-definition-id")
                .journeyCode("TEST_JOURNEY")
                .name("Test Journey")
                .version(1)
                .active(true)
                .initialState(startState)
                .states(List.of(startState, endState))
                .transitions(List.of(transition))
                .build();
    }

    private JourneyDefinitionDocument createTestJourneyDefinitionDocument() {
        JourneyDefinitionDocument.StateDocument startStateDoc = 
                new JourneyDefinitionDocument.StateDocument("START", "regular");
        JourneyDefinitionDocument.StateDocument endStateDoc = 
                new JourneyDefinitionDocument.StateDocument("END", "final");
        
        JourneyDefinitionDocument.TransitionDocument transitionDoc = 
                new JourneyDefinitionDocument.TransitionDocument(
                        "transition-1", 
                        "START", 
                        "END", 
                        "COMPLETE", 
                        null, 
                        java.util.Map.of("key", "value")
                );

        return new JourneyDefinitionDocument(
                "test-document-id",
                "Test Journey",
                "Test Description",
                "1",
                List.of(startStateDoc, endStateDoc),
                List.of(transitionDoc),
                java.util.Map.of("metadata", "value")
        );
    }
}
