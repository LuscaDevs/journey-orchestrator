package com.luscadevs.journeyorchestrator.domain.journeyinstance;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public class JourneyInstance {
    private String id;
    private String journeyDefinitionId;
    private Integer journeyVersion;
    private String currentState;
    private JourneyInstanceStatus status;
    private Instant createdAt;
    private Instant updatedAt;
    private List<TransitionHistory> history;
    private Map<String, Object> context;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getJourneyDefinitionId() {
        return journeyDefinitionId;
    }

    public void setJourneyDefinitionId(String journeyDefinitionId) {
        this.journeyDefinitionId = journeyDefinitionId;
    }

    public Integer getJourneyVersion() {
        return journeyVersion;
    }

    public void setJourneyVersion(Integer journeyVersion) {
        this.journeyVersion = journeyVersion;
    }

    public String getCurrentState() {
        return currentState;
    }

    public void setCurrentState(String currentState) {
        this.currentState = currentState;
    }

    public JourneyInstanceStatus getStatus() {
        return status;
    }

    public void setStatus(JourneyInstanceStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<TransitionHistory> getHistory() {
        return history;
    }

    public void setHistory(List<TransitionHistory> history) {
        this.history = history;
    }

    public Map<String, Object> getContext() {
        return context;
    }
}
