package com.cnrs.opentraduction.exception;


public class BusinessException extends RuntimeException {
    private String errorCode;

    public BusinessException(String message) {
        super(message);
    }
}
