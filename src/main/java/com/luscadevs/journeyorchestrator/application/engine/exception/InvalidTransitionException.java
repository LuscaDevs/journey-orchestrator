package com.luscadevs.journeyorchestrator.application.engine.exception;

public class InvalidTransitionException extends RuntimeException {

    public InvalidTransitionException(String message) {
        super(message);
    }

}