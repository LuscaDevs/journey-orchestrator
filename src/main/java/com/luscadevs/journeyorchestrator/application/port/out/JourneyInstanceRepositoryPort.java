package com.luscadevs.journeyorchestrator.application.port.out;

import java.util.List;
import java.util.Optional;

import com.luscadevs.journeyorchestrator.domain.journeyinstance.JourneyInstance;

public interface JourneyInstanceRepositoryPort {
    public JourneyInstance save(JourneyInstance journeyInstance);

    public Optional<JourneyInstance> findById(String instanceId);

    public List<JourneyInstance> findAll();

    public void deleteById(String instanceId);
}
