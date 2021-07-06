package com.example.conversions.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class TemperatureConversionResponse {
    
    private String inputDegreesUnit;

    private Double inputDegrees;

    private Double convertedDegrees;

    private String convertedDegreesUnit;

}
