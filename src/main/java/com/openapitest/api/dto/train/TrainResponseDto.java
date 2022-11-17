package com.openapitest.api.dto.train;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TrainResponseDto {

    // success
    private String line;
    private String number;
    private String congestionRatio;
    private String congestionCar;
    private String congestionType;

    // fail
    private Integer code;
    private String msg;
}