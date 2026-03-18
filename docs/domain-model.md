# Journey Orchestrator - Domain Model

## Overview

The Journey Orchestrator is a platform service responsible for executing configurable product journeys using a state machine model.

The service is **business-agnostic**, meaning it does not contain business rules.  
External systems send events to the orchestrator in order to move a journey instance through its lifecycle.

The orchestrator manages:

- Journey Definitions (workflow structure)
- Journey Instances (runtime execution)
- Events that trigger transitions
- Transition history for auditing

Persistence is implemented using MongoDB.

---

# Core Domain Concepts

## JourneyDefinition

Represents the **structure of a journey workflow**.

A JourneyDefinition describes the possible states of the journey and how transitions occur between them.

It is a **versioned configuration** that can evolve over time.

### Attributes

- journeyCode
- version
- initialState
- states
- transitions
- active
- createdAt

### Responsibility

- Define the workflow structure
- Define valid transitions
- Provide configuration for the state machine engine

---

## State

Represents a **step within a journey**.

Each state defines a stage in the lifecycle of a journey.

### Attributes

- name
- type

### Possible Types

- INITIAL
- INTERMEDIATE
- FINAL

### Responsibility

- Represent a stage of the workflow
- Serve as source or target for transitions

---

## Transition

Defines the rule that allows the journey to move from one state to another.

Transitions are triggered by events received by the orchestrator.

### Attributes

- sourceState
- event
- targetState

### Responsibility

- Define valid state changes
- Control the state machine flow

---

## Event

An external signal sent by another system to trigger a state transition.

Events represent **actions performed by external services**.

Examples:

- SUBMIT_APPLICATION
- APPROVE
- REJECT
- CANCEL

### Responsibility

- Trigger transitions in the state machine
- Move journey instances between states

---

## JourneyInstance

Represents the **execution of a journey for a specific context**.

Each instance is associated with a specific JourneyDefinition version.

### Attributes

- instanceId
- journeyCode
- version
- currentState
- status
- context
- createdAt
- updatedAt

### Responsibility

- Track the runtime state of a journey
- Receive events that move the journey forward

---

## TransitionHistory

Stores the historical record of state transitions.

This is important for **auditability and traceability**.

### Attributes

- instanceId
- fromState
- toState
- event
- timestamp

### Responsibility

- Record state transitions
- Provide audit trail for journey execution

---

# Domain Relationships

The relationships between the core concepts are:

JourneyDefinition
├── contains → States
└── contains → Transitions

JourneyInstance
├── references → JourneyDefinition
└── produces → TransitionHistory

Event
└── triggers → Transition

Transition
└── moves → JourneyInstance between States

---

# High Level Execution Flow

1. A JourneyDefinition is created and stored in MongoDB.

2. A client system starts a journey by creating a JourneyInstance.

3. The orchestrator loads the JourneyDefinition and initializes a state machine.

4. External systems send events to the orchestrator.

5. The state machine validates the transition.

6. The JourneyInstance state is updated.

7. The transition is recorded in TransitionHistory.

8. The process continues until a FINAL state is reached.
