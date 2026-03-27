package com.luscadevs.journeyorchestrator.unit.mongo.mapper;

import com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.document.JourneyDefinitionDocument;
import com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.mapper.JourneyDefinitionDocumentMapper;
import com.luscadevs.journeyorchestrator.domain.journey.JourneyDefinition;
import com.luscadevs.journeyorchestrator.domain.journey.State;
import com.luscadevs.journeyorchestrator.domain.journey.Transition;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for JourneyDefinitionDocumentMapper.
 * Tests bidirectional mapping between domain entities and MongoDB documents.
 */
class JourneyDefinitionDocumentMapperTest {

    private JourneyDefinitionDocumentMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new JourneyDefinitionDocumentMapper();
    }

    @Test
    @DisplayName("Should map domain entity to document")
    void shouldMapDomainToDocument() {
        // Given
        JourneyDefinition domain = createTestJourneyDefinition();

        // When
        JourneyDefinitionDocument document = mapper.toDocument(domain);

        // Then
        assertThat(document).isNotNull();
        assertThat(document.getId()).isEqualTo(domain.getId());
        assertThat(document.getName()).isEqualTo(domain.getName());
        assertThat(document.getVersion()).isEqualTo(domain.getVersion().toString());
        assertThat(document.getStates()).hasSize(domain.getStates().size());
        assertThat(document.getTransitions()).hasSize(domain.getTransitions().size());
        
        // Verify state mapping
        JourneyDefinitionDocument.StateDocument firstStateDoc = document.getStates().get(0);
        State firstState = domain.getStates().get(0);
        assertThat(firstStateDoc.getName()).isEqualTo(firstState.getName());
        
        // Verify transition mapping
        JourneyDefinitionDocument.TransitionDocument firstTransitionDoc = document.getTransitions().get(0);
        Transition firstTransition = domain.getTransitions().get(0);
        assertThat(firstTransitionDoc.getFromState()).isEqualTo(firstTransition.getSourceState().getName());
        assertThat(firstTransitionDoc.getToState()).isEqualTo(firstTransition.getTargetState().getName());
        assertThat(firstTransitionDoc.getEvent()).isEqualTo(firstTransition.getEvent().getName());
    }

    @Test
    @DisplayName("Should map document to domain entity")
    void shouldMapDocumentToDomain() {
        // Given
        JourneyDefinitionDocument document = createTestJourneyDefinitionDocument();

        // When
        JourneyDefinition domain = mapper.toDomain(document);

        // Then
        assertThat(domain).isNotNull();
        assertThat(domain.getId()).isEqualTo(document.getId());
        assertThat(domain.getName()).isEqualTo(document.getName());
        assertThat(domain.getVersion()).isEqualTo(Integer.parseInt(document.getVersion()));
        assertThat(domain.getStates()).hasSize(document.getStates().size());
        assertThat(domain.getTransitions()).hasSize(document.getTransitions().size());
        
        // Verify state mapping
        State firstState = domain.getStates().get(0);
        JourneyDefinitionDocument.StateDocument firstStateDoc = document.getStates().get(0);
        assertThat(firstState.getName()).isEqualTo(firstStateDoc.getName());
        
        // Verify transition mapping
        Transition firstTransition = domain.getTransitions().get(0);
        JourneyDefinitionDocument.TransitionDocument firstTransitionDoc = document.getTransitions().get(0);
        assertThat(firstTransition.getSourceState().getName()).isEqualTo(firstTransitionDoc.getFromState());
        assertThat(firstTransition.getTargetState().getName()).isEqualTo(firstTransitionDoc.getToState());
        assertThat(firstTransition.getEvent().getName()).isEqualTo(firstTransitionDoc.getEvent());
    }

    @Test
    @DisplayName("Should handle null domain entity")
    void shouldHandleNullDomainEntity() {
        // When
        JourneyDefinitionDocument document = mapper.toDocument(null);

        // Then
        assertThat(document).isNull();
    }

    @Test
    @DisplayName("Should handle null document")
    void shouldHandleNullDocument() {
        // When
        JourneyDefinition domain = mapper.toDomain(null);

        // Then
        assertThat(domain).isNull();
    }

    @Test
    @DisplayName("Should handle empty collections")
    void shouldHandleEmptyCollections() {
        // Given
        JourneyDefinition domain = JourneyDefinition.builder()
                .id("test-id")
                .journeyCode("TEST")
                .name("Test Journey")
                .version(1)
                .active(true)
                .states(List.of())
                .transitions(List.of())
                .build();

        // When
        JourneyDefinitionDocument document = mapper.toDocument(domain);

        // Then
        assertThat(document.getStates()).isEmpty();
        assertThat(document.getTransitions()).isEmpty();
    }

    @Test
    @DisplayName("Should handle missing metadata gracefully")
    void shouldHandleMissingMetadata() {
        // Given
        JourneyDefinition domain = createTestJourneyDefinition();
        JourneyDefinitionDocument document = mapper.toDocument(domain);
        document.setMetadata(null);

        // When
        JourneyDefinition result = mapper.toDomain(document);

        // Then
        assertThat(result).isNotNull();
        // Should not throw exception when metadata is null
    }

    @Test
    @DisplayName("Should handle invalid version format")
    void shouldHandleInvalidVersionFormat() {
        // Given
        JourneyDefinitionDocument document = createTestJourneyDefinitionDocument();
        document.setVersion("invalid-version");

        // When/Then
        assertThatThrownBy(() -> mapper.toDomain(document))
                .isInstanceOf(NumberFormatException.class);
    }

    @Test
    @DisplayName("Should round-trip mapping correctly")
    void shouldRoundTripMappingCorrectly() {
        // Given
        JourneyDefinition original = createTestJourneyDefinition();

        // When
        JourneyDefinitionDocument document = mapper.toDocument(original);
        JourneyDefinition result = mapper.toDomain(document);

        // Then
        assertThat(result.getId()).isEqualTo(original.getId());
        assertThat(result.getJourneyCode()).isEqualTo(original.getJourneyCode());
        assertThat(result.getName()).isEqualTo(original.getName());
        assertThat(result.getVersion()).isEqualTo(original.getVersion());
        assertThat(result.isActive()).isEqualTo(original.isActive());
        assertThat(result.getStates()).hasSize(original.getStates().size());
        assertThat(result.getTransitions()).hasSize(original.getTransitions().size());
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
                        Map.of("key", "value")
                );

        return new JourneyDefinitionDocument(
                "test-document-id",
                "Test Journey",
                "Test Description",
                "1",
                List.of(startStateDoc, endStateDoc),
                List.of(transitionDoc),
                Map.of("metadata", "value")
        );
    }
}
