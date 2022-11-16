package com.openapitest.api.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TrainRequestDto {

    private String subwayLine;
    private String trainY;
}