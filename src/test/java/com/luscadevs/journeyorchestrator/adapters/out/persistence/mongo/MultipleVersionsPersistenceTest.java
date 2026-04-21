package com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo;

import com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.document.JourneyDefinitionDocument;
import com.luscadevs.journeyorchestrator.adapters.out.persistence.mongo.repository.MongoJourneyDefinitionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to validate that multiple versions of journey definitions coexist without overwriting.
 */
@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("Multiple Versions Persistence Tests")
public class MultipleVersionsPersistenceTest {

    @Autowired
    private MongoJourneyDefinitionRepository repository;

    private static final String JOURNEY_CODE = "MULTI_VERSION_TEST";

    @BeforeEach
    void cleanup() {
        repository.deleteAll();
    }

    @Test
    @DisplayName("Should persist multiple versions without overwriting")
    void shouldPersistMultipleVersionsWithoutOverwriting() {
        // Create version 1
        JourneyDefinitionDocument v1 = createJourneyDefinition(1);
        JourneyDefinitionDocument savedV1 = repository.save(v1);
        assertNotNull(savedV1.getId());

        // Create version 2
        JourneyDefinitionDocument v2 = createJourneyDefinition(2);
        JourneyDefinitionDocument savedV2 = repository.save(v2);
        assertNotNull(savedV2.getId());

        // Create version 3
        JourneyDefinitionDocument v3 = createJourneyDefinition(3);
        JourneyDefinitionDocument savedV3 = repository.save(v3);
        assertNotNull(savedV3.getId());

        // Verify all versions exist
        Optional<JourneyDefinitionDocument> foundV1 =
                repository.findByJourneyCodeAndVersion(JOURNEY_CODE, 1);
        Optional<JourneyDefinitionDocument> foundV2 =
                repository.findByJourneyCodeAndVersion(JOURNEY_CODE, 2);
        Optional<JourneyDefinitionDocument> foundV3 =
                repository.findByJourneyCodeAndVersion(JOURNEY_CODE, 3);

        assertTrue(foundV1.isPresent());
        assertTrue(foundV2.isPresent());
        assertTrue(foundV3.isPresent());

        // Verify they have different IDs (no overwriting)
        assertNotEquals(savedV1.getId(), savedV2.getId());
        assertNotEquals(savedV2.getId(), savedV3.getId());
        assertNotEquals(savedV1.getId(), savedV3.getId());

        // Verify content integrity
        assertEquals(1, foundV1.get().getVersion());
        assertEquals(2, foundV2.get().getVersion());
        assertEquals(3, foundV3.get().getVersion());
    }

    @Test
    @DisplayName("Should find all versions by journey code")
    void shouldFindAllVersionsByJourneyCode() {
        // Create multiple versions
        repository.save(createJourneyDefinition(1));
        repository.save(createJourneyDefinition(2));
        repository.save(createJourneyDefinition(3));

        // Find all versions
        List<JourneyDefinitionDocument> allVersions = repository.findByJourneyCode(JOURNEY_CODE);

        assertEquals(3, allVersions.size());

        // Verify all versions are present
        assertTrue(allVersions.stream().anyMatch(doc -> doc.getVersion().equals(1)));
        assertTrue(allVersions.stream().anyMatch(doc -> doc.getVersion().equals(2)));
        assertTrue(allVersions.stream().anyMatch(doc -> doc.getVersion().equals(3)));
    }

    @Test
    @DisplayName("Should find latest version correctly")
    void shouldFindLatestVersionCorrectly() {
        // Create versions out of order
        repository.save(createJourneyDefinition(2));
        repository.save(createJourneyDefinition(1));
        repository.save(createJourneyDefinition(5));
        repository.save(createJourneyDefinition(3));

        // Find latest version
        Optional<JourneyDefinitionDocument> latest =
                repository.findFirstByJourneyCodeOrderByVersionDesc(JOURNEY_CODE);

        assertTrue(latest.isPresent());
        assertEquals(5, latest.get().getVersion()); // Should be the highest version
    }

    private JourneyDefinitionDocument createJourneyDefinition(Integer version) {
        JourneyDefinitionDocument doc = new JourneyDefinitionDocument();
        doc.setJourneyCode(JOURNEY_CODE);
        doc.setName("Test Journey v" + version);
        doc.setDescription("Test journey for version " + version);
        doc.setVersion(version);
        doc.setStatus("ATIVA");
        return doc;
    }
}
