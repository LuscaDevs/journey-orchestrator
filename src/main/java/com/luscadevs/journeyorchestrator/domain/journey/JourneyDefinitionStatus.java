package com.luscadevs.journeyorchestrator.domain.journey;

/**
 * Status of a Journey Definition.
 * A journey definition can be in one of three states:
 * - RASCUNHO: Initial state, being edited
 * - ATIVA: Active and available for creating instances
 * - INATIVA: Inactive, instances cannot be created but existing instances continue
 */
public enum JourneyDefinitionStatus {
    RASCUNHO,
    ATIVA,
    INATIVA
}
