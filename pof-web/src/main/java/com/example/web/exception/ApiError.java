package com.example.web.exception;

import lombok.Data;

@Data
public class ApiError {
    private String message;

    private String code;

    private Error error;
}
