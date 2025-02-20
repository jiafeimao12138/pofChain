package com.example.web.exception;

import lombok.Data;

@Data
public class ApiException extends RuntimeException{
    private ApiError error;

    public ApiException(ApiError apiError) {
        this.error = apiError;
    }

    public ApiException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getMessage() {
        if (error != null) {
            return error.getMessage();
        }
        return super.getMessage();
    }

}
