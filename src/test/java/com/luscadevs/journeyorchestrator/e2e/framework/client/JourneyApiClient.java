package com.luscadevs.journeyorchestrator.e2e.framework.client;

import com.luscadevs.journeyorchestrator.e2e.framework.fixtures.JourneyDefinitionFixtures;
import com.luscadevs.journeyorchestrator.e2e.framework.fixtures.EventPayloadFixtures;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;

/**
 * Fluent API client wrapper for Journey Orchestrator E2E tests. Provides type-safe methods for
 * common API operations with built-in validation.
 */
public class JourneyApiClient {

    private final RequestSpecification requestSpec;

    public JourneyApiClient(RequestSpecification requestSpec) {
        this.requestSpec = requestSpec;
    }

    // Journey Definition Operations

    /**
     * Creates a new journey definition using the simple journey fixture.
     */
    public JourneyDefinitionResponse createSimpleJourney() {
        Map<String, Object> journeyDefinition = JourneyDefinitionFixtures.simpleJourney();
        return createJourneyDefinition(journeyDefinition);
    }

    /**
     * Creates a new journey definition using the conditional journey fixture.
     */
    public JourneyDefinitionResponse createConditionalJourney() {
        Map<String, Object> journeyDefinition = JourneyDefinitionFixtures.conditionalJourney();
        return createJourneyDefinition(journeyDefinition);
    }

    /**
     * Creates a new journey definition using the multi-state journey fixture.
     */
    public JourneyDefinitionResponse createMultiStateJourney() {
        Map<String, Object> journeyDefinition = JourneyDefinitionFixtures.multiStateJourney();
        return createJourneyDefinition(journeyDefinition);
    }

    /**
     * Creates a new journey definition with the provided definition.
     */
    public JourneyDefinitionResponse createJourneyDefinition(
            Map<String, Object> journeyDefinition) {
        Response response = given().spec(requestSpec).body(journeyDefinition).post("/journeys");
        return new JourneyDefinitionResponse(response);
    }

    // Journey Instance Operations

    /**
     * Starts a new journey instance for the given journey code and version.
     */
    public JourneyInstanceResponse startJourney(String journeyCode, int version) {
        return startJourney(journeyCode, version, Map.of());
    }

    /**
     * Starts a new journey instance with the provided context.
     */
    public JourneyInstanceResponse startJourney(String journeyCode, int version,
            Map<String, Object> context) {
        Map<String, Object> request =
                Map.of("journeyCode", journeyCode, "version", version, "context", context);

        Response response = given().spec(requestSpec).body(request).post("/journey-instances");
        return new JourneyInstanceResponse(response);
    }

    /**
     * Retrieves a journey instance by ID.
     */
    public JourneyInstanceResponse getJourneyInstance(String instanceId) {
        Response response = given().spec(requestSpec).get("/journey-instances/" + instanceId);
        return new JourneyInstanceResponse(response);
    }

    /**
     * Sends an event to a journey instance using a completion event fixture.
     */
    public EventResponse sendCompletionEvent(String instanceId, String completedBy) {
        Map<String, Object> eventPayload = EventPayloadFixtures.completionEvent(completedBy);
        return sendEvent(instanceId, "COMPLETE", (Map<String, Object>) eventPayload.get("payload"));
    }

    /**
     * Sends an event to a journey instance using a process event fixture.
     */
    public EventResponse sendProcessEvent(String instanceId, double amount) {
        Map<String, Object> eventPayload = EventPayloadFixtures.processEvent(amount);
        return sendEvent(instanceId, "PROCESS", (Map<String, Object>) eventPayload.get("payload"));
    }

    /**
     * Sends an event to a journey instance using an approval event fixture.
     */
    public EventResponse sendApprovalEvent(String instanceId, String approvedBy, double amount) {
        Map<String, Object> eventPayload = EventPayloadFixtures.approvalEvent(approvedBy, amount);
        return sendEvent(instanceId, "APPROVE", (Map<String, Object>) eventPayload.get("payload"));
    }

    /**
     * Sends a custom event to a journey instance.
     */
    public EventResponse sendEvent(String instanceId, String eventType,
            Map<String, Object> payload) {
        Map<String, Object> request = Map.of("event", eventType, "payload", payload);

        Response response = given().spec(requestSpec).body(request)
                .post("/journey-instances/" + instanceId + "/events");
        return new EventResponse(response);
    }

    /**
     * Retrieves the transition history for a journey instance.
     */
    public TransitionHistoryResponse getTransitionHistory(String instanceId) {
        Response response =
                given().spec(requestSpec).get("/journey-instances/" + instanceId + "/history");
        return new TransitionHistoryResponse(response);
    }

    // Fluent Builder Methods

    /**
     * Creates a new journey definition builder.
     */
    public JourneyDefinitionBuilder journeyDefinition() {
        return new JourneyDefinitionBuilder(this);
    }

    /**
     * Creates a new journey instance builder.
     */
    public JourneyInstanceBuilder journeyInstance() {
        return new JourneyInstanceBuilder(this);
    }

    /**
     * Creates a new event builder.
     */
    public EventBuilder event() {
        return new EventBuilder(this);
    }

    // Response Wrapper Classes

    public static class JourneyDefinitionResponse {
        private final Response response;

        public JourneyDefinitionResponse(Response response) {
            this.response = response;
        }

        public String getJourneyCode() {
            return response.jsonPath().getString("journeyCode");
        }

        public int getVersion() {
            return response.jsonPath().getInt("version");
        }

        public String getName() {
            return response.jsonPath().getString("name");
        }

        public boolean isActive() {
            return response.jsonPath().getBoolean("active");
        }

        public Response getRawResponse() {
            return response;
        }

        public JourneyDefinitionResponse assertCreated() {
            response.then().statusCode(201);
            return this;
        }

        public JourneyDefinitionResponse assertSuccess() {
            response.then().statusCode(anyOf(equalTo(200), equalTo(201)));
            return this;
        }
    }

    public static class JourneyInstanceResponse {
        private final Response response;

        public JourneyInstanceResponse(Response response) {
            this.response = response;
        }

        public String getInstanceId() {
            return response.jsonPath().getString("instanceId");
        }

        public String getJourneyCode() {
            return response.jsonPath().getString("journeyCode");
        }

        public String getCurrentState() {
            return response.jsonPath().getString("currentState");
        }

        public String getStatus() {
            return response.jsonPath().getString("status");
        }

        public int getVersion() {
            return response.jsonPath().getInt("version");
        }

        public Response getRawResponse() {
            return response;
        }

        public JourneyInstanceResponse assertStarted() {
            response.then().statusCode(200);
            return this;
        }

        public JourneyInstanceResponse assertState(String expectedState) {
            response.then().body("currentState", equalTo(expectedState));
            return this;
        }
    }

    public static class EventResponse {
        private final Response response;

        public EventResponse(Response response) {
            this.response = response;
        }

        public Response getRawResponse() {
            return response;
        }

        public EventResponse assertProcessed() {
            response.then().statusCode(anyOf(equalTo(200), equalTo(202)));
            return this;
        }

        public EventResponse assertRejected() {
            response.then().statusCode(anyOf(equalTo(400), equalTo(422)));
            return this;
        }
    }

    public static class TransitionHistoryResponse {
        private final Response response;

        public TransitionHistoryResponse(Response response) {
            this.response = response;
        }

        public Response getRawResponse() {
            return response;
        }

        public TransitionHistoryResponse assertRetrieved() {
            response.then().statusCode(200);
            return this;
        }
    }

    // Builder Classes

    public static class JourneyDefinitionBuilder {
        private final JourneyApiClient client;
        private Map<String, Object> journeyDefinition;

        public JourneyDefinitionBuilder(JourneyApiClient client) {
            this.client = client;
            this.journeyDefinition =
                    new java.util.HashMap<>(JourneyDefinitionFixtures.simpleJourney());
        }

        public JourneyDefinitionBuilder withCode(String code) {
            journeyDefinition.put("journeyCode", code);
            return this;
        }

        public JourneyDefinitionBuilder withName(String name) {
            journeyDefinition.put("name", name);
            return this;
        }

        public JourneyDefinitionBuilder withDescription(String description) {
            journeyDefinition.put("description", description);
            return this;
        }

        public JourneyDefinitionBuilder active(boolean active) {
            journeyDefinition.put("active", active);
            return this;
        }

        public JourneyDefinitionResponse create() {
            return client.createJourneyDefinition(journeyDefinition);
        }
    }

    public static class JourneyInstanceBuilder {
        private final JourneyApiClient client;
        private String journeyCode;
        private int version = 1;
        private Map<String, Object> context = Map.of();

        public JourneyInstanceBuilder(JourneyApiClient client) {
            this.client = client;
        }

        public JourneyInstanceBuilder forJourney(String journeyCode) {
            this.journeyCode = journeyCode;
            return this;
        }

        public JourneyInstanceBuilder version(int version) {
            this.version = version;
            return this;
        }

        public JourneyInstanceBuilder withContext(Map<String, Object> context) {
            this.context = context;
            return this;
        }

        public JourneyInstanceBuilder withContext(String key, Object value) {
            this.context = Map.of(key, value);
            return this;
        }

        public JourneyInstanceResponse start() {
            return client.startJourney(journeyCode, version, context);
        }
    }

    public static class EventBuilder {
        private final JourneyApiClient client;
        private String instanceId;
        private String eventType;
        private Map<String, Object> payload = Map.of();

        public EventBuilder(JourneyApiClient client) {
            this.client = client;
        }

        public EventBuilder toInstance(String instanceId) {
            this.instanceId = instanceId;
            return this;
        }

        public EventBuilder type(String eventType) {
            this.eventType = eventType;
            return this;
        }

        public EventBuilder withPayload(Map<String, Object> payload) {
            this.payload = payload;
            return this;
        }

        public EventBuilder withPayload(String key, Object value) {
            this.payload = Map.of(key, value);
            return this;
        }

        public EventResponse send() {
            return client.sendEvent(instanceId, eventType, payload);
        }
    }
}
