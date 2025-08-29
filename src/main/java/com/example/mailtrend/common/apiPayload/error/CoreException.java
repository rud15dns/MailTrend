package com.example.mailtrend.common.apiPayload.error;

import lombok.Getter;

@Getter
public class CoreException extends RuntimeException {

    private final ErrorType errorType;

    public CoreException(ErrorType errorType) {
        super(errorType.getMessage());
        this.errorType = errorType;
    }

}