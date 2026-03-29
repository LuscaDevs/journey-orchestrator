package com.luscadevs.journeyorchestrator.adapters.out.memory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.luscadevs.journeyorchestrator.application.port.out.JourneyInstanceRepositoryPort;
import com.luscadevs.journeyorchestrator.domain.journeyinstance.JourneyInstance;

@Repository
public class InMemoryJourneyInstanceRepository implements JourneyInstanceRepositoryPort {
    private final Map<String, JourneyInstance> storage = new HashMap<>();

    @Override
    public JourneyInstance save(JourneyInstance journeyInstance) {
        storage.put(journeyInstance.getId(), journeyInstance);
        return journeyInstance;
    }

    @Override
    public Optional<JourneyInstance> findById(String instanceId) {
        return Optional.ofNullable(storage.get(instanceId));
    }

    @Override
    public List<JourneyInstance> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public void deleteById(String instanceId) {
        storage.remove(instanceId);
    }

}
