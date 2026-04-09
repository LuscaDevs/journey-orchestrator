package com.luscadevs.journeyorchestrator.domain.engine;

import org.springframework.stereotype.Component;

import com.luscadevs.journeyorchestrator.domain.journey.Event;
import com.luscadevs.journeyorchestrator.domain.journey.JourneyDefinition;
import com.luscadevs.journeyorchestrator.domain.journeyinstance.JourneyInstance;

/**
 * Engine simplificada que apenas delega para a entidade JourneyInstance. Toda a lógica de negócio
 * está centralizada na entidade de domínio.
 */
@Component
public class JourneyEngine {

    /**
     * Aplica um evento à instância de jornada delegando para a entidade.
     * 
     * @param journeyInstance Instância da jornada
     * @param journeyDefinition Definição da jornada
     * @param event Evento a ser aplicado
     * @param eventData Dados do evento
     */
    public void applyEvent(JourneyInstance journeyInstance, JourneyDefinition journeyDefinition,
            Event event, Object eventData) {
        // Delegar toda a lógica para a entidade de domínio
        journeyInstance.applyEvent(journeyDefinition, event, eventData);
    }

}
