package com.gatewayb2.common.exception;

import org.springframework.http.HttpStatus;

/** Exceção para violações de regra de negócio (ex: saldo insuficiente, KYC não aprovado). */
public class BusinessException extends RuntimeException {

    private final String errorCode;
    private final HttpStatus status;

    public BusinessException(String errorCode, String message) {
        this(errorCode, message, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    public BusinessException(String errorCode, String message, HttpStatus status) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
