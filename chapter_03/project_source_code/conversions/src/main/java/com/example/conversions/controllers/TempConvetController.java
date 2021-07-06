package com.example.conversions.controllers;

import com.example.conversions.models.TemperatureConversionResponse;
import com.example.conversions.services.TemperatureConversionService;
import com.example.conversions.utils.OsFunctions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
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

    @GetMapping("/convert/c-to-f/{degrees}")
    public TemperatureConversionResponse convertCtoF(@PathVariable String degrees) {
        Double degreesFahrenheit  = temperatureConversionService.celsiusToFahrenheit(Double.parseDouble(degrees));
        log.info("[" + osFunctions.getHostname() + "] " + degrees + " celsius is " + degreesFahrenheit + " degrees fahrenheit");
        return new TemperatureConversionResponse("celsius", Double.parseDouble(degrees), degreesFahrenheit, "fahrenheit");
    }

    @GetMapping("/convert/f-to-c/{degrees}")
    public TemperatureConversionResponse convertFtoC(@PathVariable String degrees) {
        Double degreesCelsius  =  temperatureConversionService.fahrenheitToCelsius(Double.parseDouble(degrees));
        log.info("[" + osFunctions.getHostname() + "] " + degrees + " fahrenheit is " + degreesCelsius + " degrees celsius");
        return new TemperatureConversionResponse("fahrenheit", Double.parseDouble(degrees), degreesCelsius, "celsius");
    }

}
