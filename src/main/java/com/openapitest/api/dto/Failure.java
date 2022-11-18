package com.openapitest.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Failure<T> implements Result {

    private T data;
    private String msg;

    public Failure(T data) {
        this.data = data;
    }

    public Failure(T data, String msg) {
        this.data = data;
        this.msg = msg;
    }
}