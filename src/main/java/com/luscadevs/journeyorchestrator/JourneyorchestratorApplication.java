package com.luscadevs.journeyorchestrator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication
@ComponentScan(excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.luscadevs\\.journeyorchestrator\\.infrastructure\\..*"))
public class JourneyorchestratorApplication {

	public static void main(String[] args) {
		SpringApplication.run(JourneyorchestratorApplication.class, args);
	}

}
