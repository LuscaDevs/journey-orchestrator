package com.luscadevs.journeyorchestrator.unit.mongo.repository;

import com.luscadevs.journey.api.generated.model.JourneyStatus;
import com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.document.JourneyInstanceDocument;
import com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.mapper.JourneyInstanceDocumentMapper;
import com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.repository.JourneyInstanceRepositoryImpl;
import com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.repository.MongoJourneyInstanceRepository;
import com.luscadevs.journeyorchestrator.domain.journey.State;
import com.luscadevs.journeyorchestrator.domain.journeyinstance.JourneyInstance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for JourneyInstanceRepositoryImpl.
 * Tests repository operations and mapping integration.
 */
@ExtendWith(MockitoExtension.class)
class JourneyInstanceRepositoryImplTest {

    @Mock
    private MongoJourneyInstanceRepository mongoJourneyInstanceRepository;

    @Mock
    private JourneyInstanceDocumentMapper mapper;

    private JourneyInstanceRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new JourneyInstanceRepositoryImpl(mongoJourneyInstanceRepository, mapper);
    }

    @Test
    @DisplayName("Should save journey instance")
    void shouldSaveJourneyInstance() {
        // Given
        JourneyInstance domain = createTestJourneyInstance();
        JourneyInstanceDocument document = createTestJourneyInstanceDocument();
        JourneyInstanceDocument savedDocument = createTestJourneyInstanceDocument();
        JourneyInstance expectedDomain = createTestJourneyInstance();

        when(mapper.toDocument(domain)).thenReturn(document);
        when(mongoJourneyInstanceRepository.save(document)).thenReturn(savedDocument);
        when(mapper.toDomain(savedDocument)).thenReturn(expectedDomain);

        // When
        JourneyInstance result = repository.save(domain);

        // Then
        assertThat(result).isEqualTo(expectedDomain);
        verify(mapper).toDocument(domain);
        verify(mongoJourneyInstanceRepository).save(document);
        verify(mapper).toDomain(savedDocument);
    }

    @Test
    @DisplayName("Should find journey instance by ID")
    void shouldFindById() {
        // Given
        String instanceId = "test-instance-id";
        JourneyInstanceDocument document = createTestJourneyInstanceDocument();
        JourneyInstance expectedDomain = createTestJourneyInstance();

        when(mongoJourneyInstanceRepository.findById(instanceId)).thenReturn(Optional.of(document));
        when(mapper.toDomain(document)).thenReturn(expectedDomain);

        // When
        Optional<JourneyInstance> result = repository.findById(instanceId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(expectedDomain);
        verify(mongoJourneyInstanceRepository).findById(instanceId);
        verify(mapper).toDomain(document);
    }

    @Test
    @DisplayName("Should return empty when journey instance not found by ID")
    void shouldReturnEmptyWhenNotFoundById() {
        // Given
        String instanceId = "non-existent-id";

        when(mongoJourneyInstanceRepository.findById(instanceId)).thenReturn(Optional.empty());

        // When
        Optional<JourneyInstance> result = repository.findById(instanceId);

        // Then
        assertThat(result).isEmpty();
        verify(mongoJourneyInstanceRepository).findById(instanceId);
        verify(mapper, never()).toDomain(any());
    }

    @Test
    @DisplayName("Should delete journey instance by ID")
    void shouldDeleteById() {
        // Given
        String instanceId = "test-instance-id";

        // When
        repository.deleteById(instanceId);

        // Then
        verify(mongoJourneyInstanceRepository).deleteById(instanceId);
    }

    @Test
    @DisplayName("Should handle null journey instance save")
    void shouldHandleNullJourneyInstanceSave() {
        // Given
        JourneyInstance domain = null;

        // When
        JourneyInstance result = repository.save(domain);

        // Then
        assertThat(result).isNull();
        verify(mapper, never()).toDocument(any());
        verify(mongoJourneyInstanceRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should handle null instance ID for find")
    void shouldHandleNullInstanceIdForFind() {
        // Given
        String instanceId = null;

        when(mongoJourneyInstanceRepository.findById(instanceId)).thenReturn(Optional.empty());

        // When
        Optional<JourneyInstance> result = repository.findById(instanceId);

        // Then
        assertThat(result).isEmpty();
        verify(mongoJourneyInstanceRepository).findById(instanceId);
        verify(mapper, never()).toDomain(any());
    }

    @Test
    @DisplayName("Should handle null instance ID for delete")
    void shouldHandleNullInstanceIdForDelete() {
        // Given
        String instanceId = null;

        // When
        repository.deleteById(instanceId);

        // Then
        verify(mongoJourneyInstanceRepository).deleteById(instanceId);
    }

    @Test
    @DisplayName("Should handle mapping exceptions on save")
    void shouldHandleMappingExceptionsOnSave() {
        // Given
        JourneyInstance domain = createTestJourneyInstance();

        when(mapper.toDocument(domain)).thenThrow(new RuntimeException("Mapping error"));

        // When/Then
        assertThatThrownBy(() -> repository.save(domain))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Mapping error");
        verify(mapper).toDocument(domain);
        verify(mongoJourneyInstanceRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should handle repository exceptions on save")
    void shouldHandleRepositoryExceptionsOnSave() {
        // Given
        JourneyInstance domain = createTestJourneyInstance();
        JourneyInstanceDocument document = createTestJourneyInstanceDocument();

        when(mapper.toDocument(domain)).thenReturn(document);
        when(mongoJourneyInstanceRepository.save(document))
                .thenThrow(new RuntimeException("Repository error"));

        // When/Then
        assertThatThrownBy(() -> repository.save(domain))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Repository error");
        verify(mapper).toDocument(domain);
        verify(mongoJourneyInstanceRepository).save(document);
    }

    @Test
    @DisplayName("Should handle mapping exceptions on find")
    void shouldHandleMappingExceptionsOnFind() {
        // Given
        String instanceId = "test-instance-id";
        JourneyInstanceDocument document = createTestJourneyInstanceDocument();

        when(mongoJourneyInstanceRepository.findById(instanceId)).thenReturn(Optional.of(document));
        when(mapper.toDomain(document)).thenThrow(new RuntimeException("Mapping error"));

        // When/Then
        assertThatThrownBy(() -> repository.findById(instanceId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Mapping error");
        verify(mongoJourneyInstanceRepository).findById(instanceId);
        verify(mapper).toDomain(document);
    }

    @Test
    @DisplayName("Should handle repository exceptions on find")
    void shouldHandleRepositoryExceptionsOnFind() {
        // Given
        String instanceId = "test-instance-id";

        when(mongoJourneyInstanceRepository.findById(instanceId))
                .thenThrow(new RuntimeException("Repository error"));

        // When/Then
        assertThatThrownBy(() -> repository.findById(instanceId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Repository error");
        verify(mongoJourneyInstanceRepository).findById(instanceId);
        verify(mapper, never()).toDomain(any());
    }

    @Test
    @DisplayName("Should handle repository exceptions on delete")
    void shouldHandleRepositoryExceptionsOnDelete() {
        // Given
        String instanceId = "test-instance-id";

        doThrow(new RuntimeException("Repository error"))
                .when(mongoJourneyInstanceRepository).deleteById(instanceId);

        // When/Then
        assertThatThrownBy(() -> repository.deleteById(instanceId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Repository error");
        verify(mongoJourneyInstanceRepository).deleteById(instanceId);
    }

    @Test
    @DisplayName("Should round-trip journey instance correctly")
    void shouldRoundTripJourneyInstanceCorrectly() {
        // Given
        JourneyInstance original = createTestJourneyInstance();
        JourneyInstanceDocument document = createTestJourneyInstanceDocument();
        JourneyInstanceDocument savedDocument = createTestJourneyInstanceDocument();

        when(mapper.toDocument(original)).thenReturn(document);
        when(mongoJourneyInstanceRepository.save(document)).thenReturn(savedDocument);
        when(mapper.toDomain(savedDocument)).thenReturn(original);

        // When
        JourneyInstance saved = repository.save(original);
        Optional<JourneyInstance> retrieved = repository.findById(saved.getId());

        // Then
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getId()).isEqualTo(original.getId());
        assertThat(retrieved.get().getJourneyDefinitionId()).isEqualTo(original.getJourneyDefinitionId());
        assertThat(retrieved.get().getCurrentState().getName()).isEqualTo(original.getCurrentState().getName());
        assertThat(retrieved.get().getStatus()).isEqualTo(original.getStatus());
    }

    private JourneyInstance createTestJourneyInstance() {
        State currentState = State.builder().name("START").build();
        
        return JourneyInstance.builder()
                .id("test-instance-id")
                .journeyDefinitionId("test-definition-id")
                .journeyVersion(1)
                .currentState(currentState)
                .status(JourneyStatus.RUNNING)
                .createdAt(Instant.parse("2023-01-01T00:00:00Z"))
                .updatedAt(Instant.parse("2023-01-01T01:00:00Z"))
                .history(List.of())
                .context(java.util.Map.of("key", "value", "userId", "test-user"))
                .build();
    }

    private JourneyInstanceDocument createTestJourneyInstanceDocument() {
        JourneyInstanceDocument document = new JourneyInstanceDocument();
        document.setId("test-document-id");
        document.setJourneyDefinitionId("test-definition-id");
        document.setCurrentState("START");
        document.setStatus("RUNNING");
        document.setContext(java.util.Map.of("key", "value", "userId", "test-user"));
        document.setStartedAt("2023-01-01T00:00:00Z");
        document.setLastActivityAt("2023-01-01T01:00:00Z");
        return document;
    }
}
