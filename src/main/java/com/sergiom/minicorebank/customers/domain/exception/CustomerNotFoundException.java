package com.sergiom.minicorebank.customers.domain.exception;

import java.util.UUID;

/** 404 cuando el cliente no existe. */
public class CustomerNotFoundException extends RuntimeException {
    public CustomerNotFoundException(UUID id) {
        super("customer not found: " + id);
    }
}

