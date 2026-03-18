# Journey Definition DSL

## Overview

The Journey Definition DSL describes the structure of a journey executed by the Journey Orchestrator.

A journey definition specifies:

- States
- Transitions
- Initial state
- Versioning information

This document is stored in MongoDB and dynamically loaded by the orchestrator to build the state machine.

The orchestrator remains business-agnostic and only executes the workflow defined by the DSL.

---

# Journey Definition Structure

A journey definition contains the following attributes:

| Field        | Description                           |
| ------------ | ------------------------------------- |
| journeyCode  | Unique identifier of the journey      |
| version      | Version of the journey definition     |
| initialState | State where the journey starts        |
| states       | List of possible states               |
| transitions  | Allowed transitions                   |
| active       | Indicates if the definition is active |
| createdAt    | Creation timestamp                    |

---

# State Structure

A state represents a step in the journey lifecycle.

## Attributes

| Field | Description              |
| ----- | ------------------------ |
| name  | Unique name of the state |
| type  | Type of the state        |

## Possible Types

- INITIAL
- INTERMEDIATE
- FINAL

Example:

    {
      "name": "ANALYSIS",
      "type": "INTERMEDIATE"
    }

---

# Transition Structure

A transition defines how the journey moves from one state to another.

Transitions are triggered by events sent by external systems.

## Attributes

| Field  | Description                        |
| ------ | ---------------------------------- |
| source | Source state                       |
| event  | Event that triggers the transition |
| target | Target state                       |

Example:

    {
      "source": "ANALYSIS",
      "event": "APPROVE",
      "target": "APPROVED"
    }

---

# Complete Example

Example of a loan journey definition:

    {
      "journeyCode": "personal-loan",
      "version": "1",
      "initialState": "START",
      "states": [
        {
          "name": "START",
          "type": "INITIAL"
        },
        {
          "name": "ANALYSIS",
          "type": "INTERMEDIATE"
        },
        {
          "name": "APPROVED",
          "type": "FINAL"
        },
        {
          "name": "REJECTED",
          "type": "FINAL"
        }
      ],
      "transitions": [
        {
          "source": "START",
          "event": "SUBMIT",
          "target": "ANALYSIS"
        },
        {
          "source": "ANALYSIS",
          "event": "APPROVE",
          "target": "APPROVED"
        },
        {
          "source": "ANALYSIS",
          "event": "REJECT",
          "target": "REJECTED"
        }
      ],
      "active": true,
      "createdAt": "2026-03-18T00:00:00Z"
    }

---

# Versioning Strategy

Journey definitions support versioning.

A journey is uniquely identified by:

    journeyCode + version

Example:

- personal-loan v1
- personal-loan v2

Existing journey instances always execute using the version they were created with.

---

# Execution Model

Execution flow:

1. A JourneyDefinition is loaded from MongoDB.
2. The orchestrator builds a state machine dynamically.
3. A JourneyInstance is created.
4. External systems send events.
5. The state machine processes the event.
6. The instance state is updated.
7. Transition history is recorded.

---

# Design Principles

The DSL follows these principles:

- Business-agnostic orchestration
- Declarative workflow definition
- Versioned configurations
- External event-driven transitions
