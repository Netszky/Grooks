package com.netszky.grooks.common.error;

import org.springframework.http.HttpStatus;

public abstract class BusinessException extends RuntimeException {

    private final String code;

    private final HttpStatus status;


    protected BusinessException(String code, String message,  HttpStatus status) {
        this.code = code;
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }
}
