package com.openapitest.api.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Response {

    private boolean isSuccess;
    private int code;
    private Result result;
    private HttpStatus status;

    public static Response success(HttpStatus status) {
        return new Response(true, 0, null, status);
    }

    public static <T> Response success(T data, HttpStatus status) {
        return new Response(true, 0, new Success<>(data), status);
    }

    public static <T> Response failure(int code, T data, String msg, HttpStatus status) {
        return new Response(false, code, new Failure<>(data, msg), status);
    }
}