package com.luscadevs.journeyorchestrator.domain.journey;

import com.luscadevs.journeyorchestrator.domain.connector.ConnectorConfiguration;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "name")
public class State {

    private UUID id; // Unique identifier for the state
    private String name;
    private StateType type;
    private Position position; // Visual editor position data (optional)
    private ConnectorConfiguration connectorConfiguration; // Connector configuration for SERVICE_TASK states

    /**
     * Validates the state configuration.
     * 
     * @throws IllegalArgumentException if validation fails
     */
    public void validate() {
        if (type == StateType.SERVICE_TASK && connectorConfiguration == null) {
            throw new IllegalArgumentException(
                    "Connector configuration is required when state type is SERVICE_TASK");
        }
        if (type != StateType.SERVICE_TASK && connectorConfiguration != null) {
            throw new IllegalArgumentException(
                    "Connector configuration is only allowed for SERVICE_TASK states");
        }
    }
}
