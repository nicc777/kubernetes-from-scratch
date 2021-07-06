package com.example.conversions.services;

import org.springframework.stereotype.Component;

@Component
public class TemperatureConversionServiceImpl implements TemperatureConversionService {

    @Override
    public Double celsiusToFahrenheit(Double degrees) {
        return (degrees * 9/5) + 32;
    }

    @Override
    public Double fahrenheitToCelsius(Double degrees) {
        return  (degrees - 32) * 5/9;
    }
    
}
