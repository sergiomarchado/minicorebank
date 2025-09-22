package com.sergiom.minicorebank.accounts.domain.exception;

import java.util.UUID;

/**
 * Se lanza cuando una cuenta no existe.
 * Ayuda a devolver un 404 claro a la API.
 */
public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(UUID id) {
        super("account not found: " + id);
    }
}
