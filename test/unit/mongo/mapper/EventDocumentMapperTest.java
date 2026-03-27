package com.luscadevs.journeyorchestrator.unit.mongo.mapper;

import com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.document.EventDocument;
import com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.mapper.EventDocumentMapper;
import com.luscadevs.journeyorchestrator.domain.journey.Event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for EventDocumentMapper.
 * Tests bidirectional mapping between domain entities and MongoDB documents.
 */
class EventDocumentMapperTest {

    private EventDocumentMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new EventDocumentMapper();
    }

    @Test
    @DisplayName("Should map domain entity to document")
    void shouldMapDomainToDocument() {
        // Given
        Event domain = createTestEvent();

        // When
        EventDocument document = mapper.toDocument(domain);

        // Then
        assertThat(document).isNotNull();
        assertThat(document.getEventType()).isEqualTo(domain.getName());
        assertThat(document.getEventData()).isEqualTo(domain.getMetadata());
        assertThat(document.getMetadata()).isEqualTo(domain.getMetadata());
        assertThat(document.getTimestamp()).isNotNull();
        assertThat(document.getJourneyInstanceId()).isNull();
        assertThat(document.getPreviousState()).isNull();
        assertThat(document.getNewState()).isNull();
        assertThat(document.getContext()).isNotNull();
        assertThat(document.getUserId()).isNull();
    }

    @Test
    @DisplayName("Should map document to domain entity")
    void shouldMapDocumentToDomain() {
        // Given
        EventDocument document = createTestEventDocument();

        // When
        Event domain = mapper.toDomain(document);

        // Then
        assertThat(domain).isNotNull();
        assertThat(domain.getName()).isEqualTo(document.getEventType());
        assertThat(domain.getMetadata()).isEqualTo(document.getEventData());
        assertThat(domain.getDescription()).isNull(); // Description not available in document
    }

    @Test
    @DisplayName("Should handle null domain entity")
    void shouldHandleNullDomainEntity() {
        // When
        EventDocument document = mapper.toDocument(null);

        // Then
        assertThat(document).isNull();
    }

    @Test
    @DisplayName("Should handle null document")
    void shouldHandleNullDocument() {
        // When
        Event domain = mapper.toDomain(null);

        // Then
        assertThat(domain).isNull();
    }

    @Test
    @DisplayName("Should handle event with null metadata")
    void shouldHandleEventWithNullMetadata() {
        // Given
        Event domain = Event.builder()
                .name("test-event")
                .metadata(null)
                .build();

        // When
        EventDocument document = mapper.toDocument(domain);

        // Then
        assertThat(document).isNotNull();
        assertThat(document.getEventData()).isNotNull();
        assertThat(document.getMetadata()).isNotNull();
    }

    @Test
    @DisplayName("Should handle document with null event data")
    void shouldHandleDocumentWithNullEventData() {
        // Given
        EventDocument document = createTestEventDocument();
        document.setEventData(null);

        // When
        Event domain = mapper.toDomain(document);

        // Then
        assertThat(domain).isNotNull();
        assertThat(domain.getMetadata()).isNotNull();
    }

    @Test
    @DisplayName("Should handle document with null metadata")
    void shouldHandleDocumentWithNullMetadata() {
        // Given
        EventDocument document = createTestEventDocument();
        document.setMetadata(null);

        // When
        Event domain = mapper.toDomain(document);

        // Then
        assertThat(domain).isNotNull();
        assertThat(domain.getMetadata()).isNotNull();
    }

    @Test
    @DisplayName("Should round-trip mapping correctly")
    void shouldRoundTripMappingCorrectly() {
        // Given
        Event original = createTestEvent();

        // When
        EventDocument document = mapper.toDocument(original);
        Event result = mapper.toDomain(document);

        // Then
        assertThat(result.getName()).isEqualTo(original.getName());
        assertThat(result.getMetadata()).isEqualTo(original.getMetadata());
        // Note: Description is not preserved in current mapping
    }

    @Test
    @DisplayName("Should handle event created with static factory method")
    void shouldHandleEventCreatedWithStaticFactoryMethod() {
        // Given
        Event domain = Event.of("factory-event");

        // When
        EventDocument document = mapper.toDocument(domain);

        // Then
        assertThat(document).isNotNull();
        assertThat(document.getEventType()).isEqualTo("factory-event");
        assertThat(document.getEventData()).isNotNull();
    }

    @Test
    @DisplayName("Should handle event created with constructor")
    void shouldHandleEventCreatedWithConstructor() {
        // Given
        Event domain = new Event("constructor-event");

        // When
        EventDocument document = mapper.toDocument(domain);

        // Then
        assertThat(document).isNotNull();
        assertThat(document.getEventType()).isEqualTo("constructor-event");
    }

    @Test
    @DisplayName("Should generate current timestamp")
    void shouldGenerateCurrentTimestamp() {
        // Given
        Event domain = createTestEvent();
        Instant beforeMapping = Instant.now();

        // When
        EventDocument document = mapper.toDocument(domain);
        Instant afterMapping = Instant.now();

        // Then
        assertThat(document.getTimestamp()).isNotNull();
        Instant documentTimestamp = Instant.parse(document.getTimestamp());
        assertThat(documentTimestamp).isBetween(beforeMapping, afterMapping);
    }

    @Test
    @DisplayName("Should handle empty metadata")
    void shouldHandleEmptyMetadata() {
        // Given
        Event domain = Event.builder()
                .name("test-event")
                .metadata(Map.of())
                .build();

        // When
        EventDocument document = mapper.toDocument(domain);

        // Then
        assertThat(document).isNotNull();
        assertThat(document.getEventData()).isNotNull();
        assertThat(document.getEventData()).isEmpty();
    }

    @Test
    @DisplayName("Should preserve complex metadata")
    void shouldPreserveComplexMetadata() {
        // Given
        Map<String, Object> complexMetadata = Map.of(
                "string", "value",
                "number", 42,
                "boolean", true,
                "nested", Map.of("key", "nested-value")
        );
        Event domain = Event.builder()
                .name("complex-event")
                .metadata(complexMetadata)
                .build();

        // When
        EventDocument document = mapper.toDocument(domain);
        Event result = mapper.toDomain(document);

        // Then
        assertThat(result.getMetadata()).isEqualTo(complexMetadata);
    }

    private Event createTestEvent() {
        return Event.builder()
                .name("test-event")
                .description("Test event description")
                .metadata(Map.of("key", "value", "source", "test"))
                .build();
    }

    private EventDocument createTestEventDocument() {
        EventDocument document = new EventDocument();
        document.setJourneyInstanceId("test-instance-id");
        document.setEventType("test-event");
        document.setPreviousState("START");
        document.setNewState("END");
        document.setEventData(Map.of("key", "value", "source", "test"));
        document.setContext(Map.of("userId", "test-user"));
        document.setTimestamp("2023-01-01T12:00:00Z");
        document.setUserId("test-user");
        document.setMetadata(Map.of("key", "value", "source", "test"));
        return document;
    }
}
