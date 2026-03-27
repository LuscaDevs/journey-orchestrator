package com.luscadevs.journeyorchestrator.unit.mongo.mapper;

import com.luscadevs.journey.api.generated.model.JourneyStatus;
import com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.document.JourneyInstanceDocument;
import com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.mapper.JourneyInstanceDocumentMapper;
import com.luscadevs.journeyorchestrator.domain.journey.State;
import com.luscadevs.journeyorchestrator.domain.journeyinstance.JourneyInstance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for JourneyInstanceDocumentMapper.
 * Tests bidirectional mapping between domain entities and MongoDB documents.
 */
class JourneyInstanceDocumentMapperTest {

    private JourneyInstanceDocumentMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new JourneyInstanceDocumentMapper();
    }

    @Test
    @DisplayName("Should map domain entity to document")
    void shouldMapDomainToDocument() {
        // Given
        JourneyInstance domain = createTestJourneyInstance();

        // When
        JourneyInstanceDocument document = mapper.toDocument(domain);

        // Then
        assertThat(document).isNotNull();
        assertThat(document.getId()).isEqualTo(domain.getId());
        assertThat(document.getJourneyDefinitionId()).isEqualTo(domain.getJourneyDefinitionId());
        assertThat(document.getCurrentState()).isEqualTo(domain.getCurrentState().getName());
        assertThat(document.getStatus()).isEqualTo(domain.getStatus().name());
        assertThat(document.getContext()).isEqualTo(domain.getContext());
        assertThat(document.getStartedAt()).isEqualTo(domain.getCreatedAt().toString());
        assertThat(document.getLastActivityAt()).isEqualTo(domain.getUpdatedAt().toString());
    }

    @Test
    @DisplayName("Should map document to domain entity")
    void shouldMapDocumentToDomain() {
        // Given
        JourneyInstanceDocument document = createTestJourneyInstanceDocument();

        // When
        JourneyInstance domain = mapper.toDomain(document);

        // Then
        assertThat(domain).isNotNull();
        assertThat(domain.getId()).isEqualTo(document.getId());
        assertThat(domain.getJourneyDefinitionId()).isEqualTo(document.getJourneyDefinitionId());
        assertThat(domain.getCurrentState().getName()).isEqualTo(document.getCurrentState());
        assertThat(domain.getStatus()).isEqualTo(JourneyStatus.valueOf(document.getStatus()));
        assertThat(domain.getContext()).isEqualTo(document.getContext());
        assertThat(domain.getCreatedAt()).isEqualTo(Instant.parse(document.getStartedAt()));
        assertThat(domain.getUpdatedAt()).isEqualTo(Instant.parse(document.getLastActivityAt()));
        assertThat(domain.getHistory()).isNotNull();
    }

    @Test
    @DisplayName("Should handle null domain entity")
    void shouldHandleNullDomainEntity() {
        // When
        JourneyInstanceDocument document = mapper.toDocument(null);

        // Then
        assertThat(document).isNull();
    }

    @Test
    @DisplayName("Should handle null document")
    void shouldHandleNullDocument() {
        // When
        JourneyInstance domain = mapper.toDomain(null);

        // Then
        assertThat(domain).isNull();
    }

    @Test
    @DisplayName("Should handle null last activity timestamp")
    void shouldHandleNullLastActivityTimestamp() {
        // Given
        JourneyInstanceDocument document = createTestJourneyInstanceDocument();
        document.setLastActivityAt(null);

        // When
        JourneyInstance domain = mapper.toDomain(document);

        // Then
        assertThat(domain).isNotNull();
        assertThat(domain.getUpdatedAt()).isNull();
    }

    @Test
    @DisplayName("Should handle null context")
    void shouldHandleNullContext() {
        // Given
        JourneyInstance domain = createTestJourneyInstance();
        JourneyInstanceDocument document = mapper.toDocument(domain);
        document.setContext(null);

        // When
        JourneyInstance result = mapper.toDomain(document);

        // Then
        assertThat(result).isNotNull();
        // Should not throw exception when context is null
    }

    @Test
    @DisplayName("Should handle invalid status format")
    void shouldHandleInvalidStatusFormat() {
        // Given
        JourneyInstanceDocument document = createTestJourneyInstanceDocument();
        document.setStatus("INVALID_STATUS");

        // When/Then
        assertThatThrownBy(() -> mapper.toDomain(document))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should handle invalid timestamp format")
    void shouldHandleInvalidTimestampFormat() {
        // Given
        JourneyInstanceDocument document = createTestJourneyInstanceDocument();
        document.setStartedAt("invalid-timestamp");

        // When/Then
        assertThatThrownBy(() -> mapper.toDomain(document))
                .isInstanceOf(java.time.format.DateTimeParseException.class);
    }

    @Test
    @DisplayName("Should round-trip mapping correctly")
    void shouldRoundTripMappingCorrectly() {
        // Given
        JourneyInstance original = createTestJourneyInstance();

        // When
        JourneyInstanceDocument document = mapper.toDocument(original);
        JourneyInstance result = mapper.toDomain(document);

        // Then
        assertThat(result.getId()).isEqualTo(original.getId());
        assertThat(result.getJourneyDefinitionId()).isEqualTo(original.getJourneyDefinitionId());
        assertThat(result.getCurrentState().getName()).isEqualTo(original.getCurrentState().getName());
        assertThat(result.getStatus()).isEqualTo(original.getStatus());
        assertThat(result.getContext()).isEqualTo(original.getContext());
        // Note: JourneyVersion is not preserved in current mapping
        assertThat(result.getHistory()).isNotNull();
    }

    @Test
    @DisplayName("Should handle completed journey instance")
    void shouldHandleCompletedJourneyInstance() {
        // Given
        JourneyInstance completedInstance = createTestJourneyInstance();
        completedInstance.complete();

        // When
        JourneyInstanceDocument document = mapper.toDocument(completedInstance);

        // Then
        assertThat(document.getStatus()).isEqualTo("COMPLETED");
    }

    @Test
    @DisplayName("Should handle cancelled journey instance")
    void shouldHandleCancelledJourneyInstance() {
        // Given
        JourneyInstance cancelledInstance = createTestJourneyInstance();
        cancelledInstance.cancel();

        // When
        JourneyInstanceDocument document = mapper.toDocument(cancelledInstance);

        // Then
        assertThat(document.getStatus()).isEqualTo("CANCELLED");
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
                .context(Map.of("key", "value", "userId", "test-user"))
                .build();
    }

    private JourneyInstanceDocument createTestJourneyInstanceDocument() {
        JourneyInstanceDocument document = new JourneyInstanceDocument();
        document.setId("test-document-id");
        document.setJourneyDefinitionId("test-definition-id");
        document.setCurrentState("START");
        document.setStatus("RUNNING");
        document.setContext(Map.of("key", "value", "userId", "test-user"));
        document.setStartedAt("2023-01-01T00:00:00Z");
        document.setLastActivityAt("2023-01-01T01:00:00Z");
        return document;
    }
}
