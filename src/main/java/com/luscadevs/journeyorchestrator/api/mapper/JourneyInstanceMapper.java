package com.luscadevs.journeyorchestrator.api.mapper;

import com.luscadevs.journeyorchestrator.api.dto.JourneyInstanceResponse;
import com.luscadevs.journeyorchestrator.domain.journeyinstance.JourneyInstance;

public class JourneyInstanceMapper {

    public static JourneyInstanceResponse toResponse(JourneyInstance instance) {

        return JourneyInstanceResponse.builder()
                .id(instance.getId())
                .journeyDefinitionId(instance.getJourneyDefinitionId())
                .journeyVersion(instance.getJourneyVersion())
                .status(instance.getStatus().name())
                .currentState(instance.getCurrentState().getName())
                .context(instance.getContext())
                .history(instance.getHistory())
                .createdAt(instance.getCreatedAt())
                .updatedAt(instance.getUpdatedAt())
                .build();
    }
}
