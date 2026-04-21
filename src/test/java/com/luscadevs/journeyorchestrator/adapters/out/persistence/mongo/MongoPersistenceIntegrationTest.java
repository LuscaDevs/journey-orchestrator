package com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo;

import com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.document.JourneyDefinitionDocument;
import com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.document.JourneyInstanceDocument;
import com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.repository.MongoJourneyDefinitionRepository;
import com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.repository.MongoJourneyInstanceRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for MongoDB persistence layer. Tests actual MongoDB operations using test
 * profile.
 */
@SpringBootTest
@ActiveProfiles("test")
class MongoPersistenceIntegrationTest {

    @Autowired
    private MongoJourneyDefinitionRepository journeyDefinitionRepository;

    @Autowired
    private MongoJourneyInstanceRepository journeyInstanceRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void setUp() {
        // Clean up database before each test
        mongoTemplate.dropCollection(JourneyDefinitionDocument.class);
        mongoTemplate.dropCollection(JourneyInstanceDocument.class);
    }

    @Test
    @DisplayName("Should persist and retrieve journey definition")
    void shouldPersistAndRetrieveJourneyDefinition() {
        // Given
        JourneyDefinitionDocument document = new JourneyDefinitionDocument();
        document.setJourneyCode("TEST_JOURNEY");
        document.setName("Test Journey");
        document.setDescription("A test journey for integration testing");
        document.setVersion(1);
        document.setStatus("ATIVA");

        // When
        JourneyDefinitionDocument saved = journeyDefinitionRepository.save(document);
        Optional<JourneyDefinitionDocument> found =
                journeyDefinitionRepository.findByJourneyCodeAndVersion("TEST_JOURNEY", 1);

        // Then
        assertNotNull(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("TEST_JOURNEY", found.get().getJourneyCode());
        assertEquals("Test Journey", found.get().getName());
        assertEquals(1, found.get().getVersion());
        assertEquals("ATIVA", found.get().getStatus());
    }

    @Test
    @DisplayName("Should persist and retrieve journey instance")
    void shouldPersistAndRetrieveJourneyInstance() {
        // Given
        JourneyInstanceDocument document = new JourneyInstanceDocument();
        document.setJourneyDefinitionId("journey-def-123");
        document.setCurrentState("STARTED");
        document.setStatus("RUNNING");
        document.setStartedAt(Instant.now().toString());
        document.setLastActivityAt(Instant.now().toString());

        // When
        JourneyInstanceDocument saved = journeyInstanceRepository.save(document);
        Optional<JourneyInstanceDocument> found = journeyInstanceRepository.findById(saved.getId());

        // Then
        assertNotNull(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("journey-def-123", found.get().getJourneyDefinitionId());
        assertEquals("STARTED", found.get().getCurrentState());
        assertEquals("RUNNING", found.get().getStatus());
    }

    @Test
    @DisplayName("Should find journey instances by status")
    void shouldFindJourneyInstancesByStatus() {
        // Given
        JourneyInstanceDocument instance1 = new JourneyInstanceDocument();
        instance1.setJourneyDefinitionId("journey-def-123");
        instance1.setCurrentState("STARTED");
        instance1.setStatus("RUNNING");
        instance1.setStartedAt(Instant.now().toString());

        JourneyInstanceDocument instance2 = new JourneyInstanceDocument();
        instance2.setJourneyDefinitionId("journey-def-456");
        instance2.setCurrentState("COMPLETED");
        instance2.setStatus("COMPLETED");
        instance2.setStartedAt(Instant.now().toString());

        journeyInstanceRepository.save(instance1);
        journeyInstanceRepository.save(instance2);

        // When
        List<JourneyInstanceDocument> runningInstances =
                journeyInstanceRepository.findByStatus("RUNNING");
        List<JourneyInstanceDocument> completedInstances =
                journeyInstanceRepository.findByStatus("COMPLETED");

        // Then
        assertEquals(1, runningInstances.size());
        assertEquals("RUNNING", runningInstances.get(0).getStatus());

        assertEquals(1, completedInstances.size());
        assertEquals("COMPLETED", completedInstances.get(0).getStatus());
    }

    @Test
    @DisplayName("Should find journey instances by journey definition ID")
    void shouldFindJourneyInstancesByJourneyDefinitionId() {
        // Given
        String journeyDefId = "journey-def-123";

        JourneyInstanceDocument instance1 = new JourneyInstanceDocument();
        instance1.setJourneyDefinitionId(journeyDefId);
        instance1.setCurrentState("STARTED");
        instance1.setStatus("RUNNING");
        instance1.setStartedAt(Instant.now().toString());

        JourneyInstanceDocument instance2 = new JourneyInstanceDocument();
        instance2.setJourneyDefinitionId("different-journey-def");
        instance2.setCurrentState("STARTED");
        instance2.setStatus("RUNNING");
        instance2.setStartedAt(Instant.now().toString());

        journeyInstanceRepository.save(instance1);
        journeyInstanceRepository.save(instance2);

        // When
        List<JourneyInstanceDocument> foundInstances =
                journeyInstanceRepository.findByJourneyDefinitionId(journeyDefId);

        // Then
        assertEquals(1, foundInstances.size());
        assertEquals(journeyDefId, foundInstances.get(0).getJourneyDefinitionId());
    }

    @Test
    @DisplayName("Should find journey instances by journey definition ID and status")
    void shouldFindJourneyInstancesByJourneyDefinitionIdAndStatus() {
        // Given
        String journeyDefId = "journey-def-123";

        JourneyInstanceDocument instance1 = new JourneyInstanceDocument();
        instance1.setJourneyDefinitionId(journeyDefId);
        instance1.setCurrentState("STARTED");
        instance1.setStatus("RUNNING");
        instance1.setStartedAt(Instant.now().toString());

        JourneyInstanceDocument instance2 = new JourneyInstanceDocument();
        instance2.setJourneyDefinitionId(journeyDefId);
        instance2.setCurrentState("COMPLETED");
        instance2.setStatus("COMPLETED");
        instance2.setStartedAt(Instant.now().toString());

        JourneyInstanceDocument instance3 = new JourneyInstanceDocument();
        instance3.setJourneyDefinitionId("different-journey-def");
        instance3.setCurrentState("STARTED");
        instance3.setStatus("RUNNING");
        instance3.setStartedAt(Instant.now().toString());

        journeyInstanceRepository.save(instance1);
        journeyInstanceRepository.save(instance2);
        journeyInstanceRepository.save(instance3);

        // When
        List<JourneyInstanceDocument> runningInstances = journeyInstanceRepository
                .findByJourneyDefinitionIdAndStatus(journeyDefId, "RUNNING");
        List<JourneyInstanceDocument> completedInstances = journeyInstanceRepository
                .findByJourneyDefinitionIdAndStatus(journeyDefId, "COMPLETED");

        // Then
        assertEquals(1, runningInstances.size());
        assertEquals(journeyDefId, runningInstances.get(0).getJourneyDefinitionId());
        assertEquals("RUNNING", runningInstances.get(0).getStatus());

        assertEquals(1, completedInstances.size());
        assertEquals(journeyDefId, completedInstances.get(0).getJourneyDefinitionId());
        assertEquals("COMPLETED", completedInstances.get(0).getStatus());
    }
}
