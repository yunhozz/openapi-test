package com.openapitest.api.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WeatherRequestDto {

    private String nx;
    private String ny;
    private String baseDate;
    private String baseTime;
}