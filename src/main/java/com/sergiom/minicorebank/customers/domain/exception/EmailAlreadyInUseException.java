package com.sergiom.minicorebank.customers.domain.exception;

/** 409 cuando intentas crear un cliente con un email ya registrado. */
public class EmailAlreadyInUseException extends RuntimeException {
    public EmailAlreadyInUseException(String email) {
        super("email already in use: " + email);
    }
}

