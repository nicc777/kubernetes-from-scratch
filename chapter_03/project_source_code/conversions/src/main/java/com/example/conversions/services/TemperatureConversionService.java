package com.example.conversions.services;

import org.springframework.stereotype.Service;

@Service
public interface TemperatureConversionService {
    
    Double celsiusToFahrenheit(Double degrees);

    Double fahrenheitToCelsius(Double degrees);

}
