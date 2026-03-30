package com.luscadevs.journeyorchestrator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.luscadevs.journeyorchestrator")
public class JourneyorchestratorApplication {

	public static void main(String[] args) {
		SpringApplication.run(JourneyorchestratorApplication.class, args);
	}

}
