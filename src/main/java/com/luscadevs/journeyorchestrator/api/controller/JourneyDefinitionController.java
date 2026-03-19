package com.luscadevs.journeyorchestrator.api.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;

import com.luscadevs.journey.api.generated.JourneysApi;
import com.luscadevs.journey.api.generated.model.CreateJourneyDefinitionRequest;
import com.luscadevs.journey.api.generated.model.JourneyDefinitionResponse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@RestController
public class JourneyDefinitionController implements JourneysApi {

    @Override
    public ResponseEntity<JourneyDefinitionResponse> createJourneyDefinition(
            @Valid CreateJourneyDefinitionRequest createJourneyDefinitionRequest) {
        // TODO Auto-generated method stub
        return JourneysApi.super.createJourneyDefinition(createJourneyDefinitionRequest);
    }

    @Override
    public ResponseEntity<List<JourneyDefinitionResponse>> getJourneyDefinitionsByCode(@NotNull String journeyCode) {
        // TODO Auto-generated method stub
        return JourneysApi.super.getJourneyDefinitionsByCode(journeyCode);
    }

    @Override
    public Optional<NativeWebRequest> getRequest() {
        // TODO Auto-generated method stub
        return JourneysApi.super.getRequest();
    }

    @Override
    public ResponseEntity<List<JourneyDefinitionResponse>> listJourneyDefinitions() {
        // TODO Auto-generated method stub
        return JourneysApi.super.listJourneyDefinitions();
    }

}
