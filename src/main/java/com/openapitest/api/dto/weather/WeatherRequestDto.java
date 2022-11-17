package com.openapitest.api.dto.weather;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WeatherRequestDto {

    private String nx;
    private String ny;
}