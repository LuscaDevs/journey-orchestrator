package com.luscadevs.journeyorchestrator.api.mapper;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import com.luscadevs.journey.api.generated.model.JourneyInstanceResponse;
import com.luscadevs.journeyorchestrator.domain.journeyinstance.JourneyInstance;

public class JourneyInstanceMapper {

    public static JourneyInstanceResponse toResponse(
            JourneyInstance instance) {
        JourneyInstanceResponse response = new JourneyInstanceResponse();

        response.instanceId(instance.getId());
        response.journeyCode(instance.getJourneyDefinitionId());
        response.version(instance.getJourneyVersion());
        response.currentState(instance.getCurrentState().getName());
        response.status(instance.getStatus());

        response.createdAt(OffsetDateTime.ofInstant(instance.getCreatedAt(), ZoneOffset.UTC));
        response.updatedAt(OffsetDateTime.ofInstant(instance.getUpdatedAt(), ZoneOffset.UTC));

        return response;
    }
}
