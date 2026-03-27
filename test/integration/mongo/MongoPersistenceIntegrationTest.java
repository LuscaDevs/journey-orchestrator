package com.luscadevs.journeyorchestrator.integration.mongo;

import com.luscadevs.journey.api.generated.model.JourneyStatus;
import com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.document.JourneyDefinitionDocument;
import com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.document.JourneyInstanceDocument;
import com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.document.EventDocument;
import com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.repository.MongoJourneyDefinitionRepository;
import com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.repository.MongoJourneyInstanceRepository;
import com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.repository.MongoEventRepository;
import com.luscadevs.journeyorchestrator.application.port.out.JourneyDefinitionRepositoryPort;
import com.luscadevs.journeyorchestrator.application.port.out.JourneyInstanceRepositoryPort;
import com.luscadevs.journeyorchestrator.domain.journey.Event;
import com.luscadevs.journeyorchestrator.domain.journey.JourneyDefinition;
import com.luscadevs.journeyorchestrator.domain.journey.State;
import com.luscadevs.journeyorchestrator.domain.journeyinstance.JourneyInstance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for MongoDB persistence layer.
 * Tests document mapping, repository operations, and data consistency.
 */
@Testcontainers
@DataMongoTest
@Import({
        com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.config.MongoAdapterConfiguration.class,
        com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.config.MongoIndexConfiguration.class
})
class MongoPersistenceIntegrationTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:6.0"));

    @DynamicPropertySource
    static void setDynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private MongoJourneyDefinitionRepository journeyDefinitionRepository;

    @Autowired
    private MongoJourneyInstanceRepository journeyInstanceRepository;

    @Autowired
    private MongoEventRepository eventRepository;

    @Autowired
    private JourneyDefinitionRepositoryPort journeyDefinitionRepositoryPort;

    @Autowired
    private JourneyInstanceRepositoryPort journeyInstanceRepositoryPort;

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void setUp() {
        // Clean up database before each test
        mongoTemplate.dropCollection(JourneyDefinitionDocument.class);
        mongoTemplate.dropCollection(JourneyInstanceDocument.class);
        mongoTemplate.dropCollection(EventDocument.class);
    }

    @Test
    @DisplayName("Should persist and retrieve journey definition")
    void shouldPersistAndRetrieveJourneyDefinition() {
        // Given
        JourneyDefinition journeyDefinition = createTestJourneyDefinition();

        // When
        JourneyDefinition saved = journeyDefinitionRepositoryPort.save(journeyDefinition);
        Optional<JourneyDefinition> retrieved = journeyDefinitionRepositoryPort.findByJourneyCodeAndVersion(
                journeyDefinition.getJourneyCode(),
                journeyDefinition.getVersion());

        // Then
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getJourneyCode()).isEqualTo(journeyDefinition.getJourneyCode());
        assertThat(retrieved.get().getVersion()).isEqualTo(journeyDefinition.getVersion());
        assertThat(retrieved.get().getName()).isEqualTo(journeyDefinition.getName());
        assertThat(retrieved.get().getStates()).hasSize(journeyDefinition.getStates().size());
    }

    @Test
    @DisplayName("Should persist and retrieve journey instance")
    void shouldPersistAndRetrieveJourneyInstance() {
        // Given
        JourneyInstance journeyInstance = createTestJourneyInstance();

        // When
        JourneyInstance saved = journeyInstanceRepositoryPort.save(journeyInstance);
        Optional<JourneyInstance> retrieved = journeyInstanceRepositoryPort.findById(saved.getId());

        // Then
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getId()).isEqualTo(saved.getId());
        assertThat(retrieved.get().getJourneyDefinitionId()).isEqualTo(journeyInstance.getJourneyDefinitionId());
        assertThat(retrieved.get().getCurrentState().getName()).isEqualTo(journeyInstance.getCurrentState().getName());
        assertThat(retrieved.get().getStatus()).isEqualTo(journeyInstance.getStatus());
    }

    @Test
    @DisplayName("Should persist events for journey instance")
    void shouldPersistEventsForJourneyInstance() {
        // Given
        Event event = Event.of("test-event");
        EventDocument eventDocument = createTestEventDocument(event);

        // When
        EventDocument saved = eventRepository.save(eventDocument);
        List<EventDocument> retrieved = eventRepository.findByJourneyInstanceId(saved.getJourneyInstanceId());

        // Then
        assertThat(retrieved).hasSize(1);
        assertThat(retrieved.get(0).getEventType()).isEqualTo(event.getName());
        assertThat(retrieved.get(0).getJourneyInstanceId()).isEqualTo(saved.getJourneyInstanceId());
    }

    @Test
    @DisplayName("Should find journey instances by status")
    void shouldFindJourneyInstancesByStatus() {
        // Given
        JourneyInstance runningInstance = createTestJourneyInstance();
        JourneyInstance completedInstance = createTestJourneyInstance();
        completedInstance.complete();

        journeyInstanceRepositoryPort.save(runningInstance);
        journeyInstanceRepositoryPort.save(completedInstance);

        // When
        List<JourneyInstanceDocument> runningInstances = journeyInstanceRepository.findByStatus("RUNNING");
        List<JourneyInstanceDocument> completedInstances = journeyInstanceRepository.findByStatus("COMPLETED");

        // Then
        assertThat(runningInstances).hasSize(1);
        assertThat(completedInstances).hasSize(1);
        assertThat(runningInstances.get(0).getStatus()).isEqualTo("RUNNING");
        assertThat(completedInstances.get(0).getStatus()).isEqualTo("COMPLETED");
    }

    @Test
    @DisplayName("Should find journey instances by definition ID")
    void shouldFindJourneyInstancesByDefinitionId() {
        // Given
        String definitionId = "test-definition-id";
        JourneyInstance instance1 = createTestJourneyInstance();
        JourneyInstance instance2 = createTestJourneyInstance();

        instance1 = JourneyInstance.builder()
                .id(instance1.getId())
                .journeyDefinitionId(definitionId)
                .journeyVersion(instance1.getJourneyVersion())
                .currentState(instance1.getCurrentState())
                .status(instance1.getStatus())
                .createdAt(instance1.getCreatedAt())
                .updatedAt(instance1.getUpdatedAt())
                .history(instance1.getHistory())
                .context(instance1.getContext())
                .build();

        instance2 = JourneyInstance.builder()
                .id(instance2.getId())
                .journeyDefinitionId(definitionId)
                .journeyVersion(instance2.getJourneyVersion())
                .currentState(instance2.getCurrentState())
                .status(instance2.getStatus())
                .createdAt(instance2.getCreatedAt())
                .updatedAt(instance2.getUpdatedAt())
                .history(instance2.getHistory())
                .context(instance2.getContext())
                .build();

        journeyInstanceRepositoryPort.save(instance1);
        journeyInstanceRepositoryPort.save(instance2);

        // When
        List<JourneyInstanceDocument> instances = journeyInstanceRepository.findByJourneyDefinitionId(definitionId);

        // Then
        assertThat(instances).hasSize(2);
        assertThat(instances).allMatch(doc -> definitionId.equals(doc.getJourneyDefinitionId()));
    }

    @Test
    @DisplayName("Should delete journey instance by ID")
    void shouldDeleteJourneyInstanceById() {
        // Given
        JourneyInstance journeyInstance = journeyInstanceRepositoryPort.save(createTestJourneyInstance());
        String instanceId = journeyInstance.getId();

        // When
        journeyInstanceRepositoryPort.deleteById(instanceId);
        Optional<JourneyInstance> retrieved = journeyInstanceRepositoryPort.findById(instanceId);

        // Then
        assertThat(retrieved).isEmpty();
    }

    @Test
    @DisplayName("Should maintain data consistency across operations")
    void shouldMaintainDataConsistencyAcrossOperations() {
        // Given
        JourneyDefinition journeyDefinition = createTestJourneyDefinition();
        JourneyInstance journeyInstance = createTestJourneyInstance();

        // When
        JourneyDefinition savedDefinition = journeyDefinitionRepositoryPort.save(journeyDefinition);
        JourneyInstance savedInstance = journeyInstanceRepositoryPort.save(journeyInstance);

        // Then
        // Verify journey definition consistency
        Optional<JourneyDefinition> retrievedDefinition = journeyDefinitionRepositoryPort.findByJourneyCodeAndVersion(
                savedDefinition.getJourneyCode(),
                savedDefinition.getVersion());
        assertThat(retrievedDefinition).isPresent();
        assertThat(retrievedDefinition.get()).isEqualTo(savedDefinition);

        // Verify journey instance consistency
        Optional<JourneyInstance> retrievedInstance = journeyInstanceRepositoryPort.findById(savedInstance.getId());
        assertThat(retrievedInstance).isPresent();
        assertThat(retrievedInstance.get()).isEqualTo(savedInstance);
    }

    private JourneyDefinition createTestJourneyDefinition() {
        State initialState = State.builder().name("START").build();
        State finalState = State.builder().name("END").build();

        return JourneyDefinition.builder()
                .id("test-definition-id")
                .journeyCode("TEST_JOURNEY")
                .name("Test Journey")
                .version(1)
                .active(true)
                .initialState(initialState)
                .states(List.of(initialState, finalState))
                .build();
    }

    private JourneyInstance createTestJourneyInstance() {
        State currentState = State.builder().name("START").build();

        return JourneyInstance.builder()
                .id("test-instance-id")
                .journeyDefinitionId("test-definition-id")
                .journeyVersion(1)
                .currentState(currentState)
                .status(JourneyStatus.RUNNING)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .context(Map.of("key", "value"))
                .build();
    }

    private EventDocument createTestEventDocument(Event event) {
        EventDocument document = new EventDocument();
        document.setJourneyInstanceId("test-instance-id");
        document.setEventType(event.getName());
        document.setEventData(Map.of("data", "value"));
        document.setContext(Map.of("context", "value"));
        document.setTimestamp(Instant.now().toString());
        document.setUserId("test-user");
        return document;
    }
}
