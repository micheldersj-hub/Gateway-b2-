package com.gatewayb2.common.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String resource, Object id) {
        super("RESOURCE_NOT_FOUND", resource + " não encontrado(a): " + id, HttpStatus.NOT_FOUND);
    }
}
