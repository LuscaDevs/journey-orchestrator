package com.luscadevs.journeyorchestrator.domain.port;

import com.luscadevs.journeyorchestrator.domain.journeyinstance.JourneyInstance;

public interface JourneyInstanceRepository {
    public JourneyInstance save(JourneyInstance journeyInstance);

    public JourneyInstance findById(String instanceId);

    public JourneyInstance findByJourneyDefinitionId(String journeyDefinitionId);

    public void deleteById(String instanceId);
}
