package com.example.conversions.controllers;

import javax.annotation.PostConstruct;

import com.example.conversions.models.TemperatureConversionResponse;
import com.example.conversions.services.ApplicationStateService;
import com.example.conversions.services.TemperatureConversionService;
import com.example.conversions.utils.OsFunctions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api")
@Slf4j
public class TempConvetController {

    @Autowired
    OsFunctions osFunctions;

    @Autowired
    TemperatureConversionService temperatureConversionService;

    @Autowired
	ApplicationStateService applicationStateService;

    @GetMapping("/liveness")
    public String liveness() {
        log.info("[" + osFunctions.getHostname() + "] liveness() called");
        return "ok";
    }

    @GetMapping("/readiness")
    public String readiness() {
        log.info("[" + osFunctions.getHostname() + "] readiness() called");
        if (!applicationStateService.isReady()){
            log.error("[" + osFunctions.getHostname() + "] readiness() - service not ready");
            throw new ServiceNotReadyException();
        }
        return "ok";
    }

    @GetMapping("/convert/c-to-f/{degrees}")
    public TemperatureConversionResponse convertCtoF(@PathVariable String degrees) {
        if (!applicationStateService.isReady())
            throw new ServiceNotReadyException();
        Double degreesFahrenheit  = temperatureConversionService.celsiusToFahrenheit(Double.parseDouble(degrees));
        log.info("[" + osFunctions.getHostname() + "] " + degrees + " celsius is " + degreesFahrenheit + " degrees fahrenheit");
        return new TemperatureConversionResponse("celsius", Double.parseDouble(degrees), degreesFahrenheit, "fahrenheit");
    }

    @GetMapping("/convert/f-to-c/{degrees}")
    public TemperatureConversionResponse convertFtoC(@PathVariable String degrees) {
        if (!applicationStateService.isReady())
            throw new ServiceNotReadyException();
        Double degreesCelsius  =  temperatureConversionService.fahrenheitToCelsius(Double.parseDouble(degrees));
        log.info("[" + osFunctions.getHostname() + "] " + degrees + " fahrenheit is " + degreesCelsius + " degrees celsius");
        return new TemperatureConversionResponse("fahrenheit", Double.parseDouble(degrees), degreesCelsius, "celsius");
    }

    @PostConstruct
    public void postConstruct(){
        try {
            applicationStateService.prepareReadyState();
        } catch (InterruptedException e) {
            log.error("EXCEPTION: ", e);
        }
    }

    @ResponseStatus(value = HttpStatus.SERVICE_UNAVAILABLE, reason="The service is not in a ready state")
    public class ServiceNotReadyException extends RuntimeException {}

}
