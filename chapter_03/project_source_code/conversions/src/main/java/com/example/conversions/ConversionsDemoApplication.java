package com.example.conversions;

import javax.annotation.PreDestroy;

import com.example.conversions.services.ApplicationStateService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ConversionsDemoApplication {

	@Autowired
	ApplicationStateService applicationStateService;

	public static void main(String[] args) {
		SpringApplication.run(ConversionsDemoApplication.class, args);
	}

	@PreDestroy
	public void tearDown() {
		try {
			applicationStateService.prepareForTermination();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

}
