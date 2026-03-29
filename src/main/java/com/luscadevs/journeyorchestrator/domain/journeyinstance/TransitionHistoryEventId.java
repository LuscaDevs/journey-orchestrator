package com.luscadevs.journeyorchestrator.domain.journeyinstance;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class TransitionHistoryEventId {
    private String value;
    
    public static TransitionHistoryEventId generate() {
        return new TransitionHistoryEventId(java.util.UUID.randomUUID().toString());
    }
    
    public static TransitionHistoryEventId of(String value) {
        return new TransitionHistoryEventId(value);
    }
}
