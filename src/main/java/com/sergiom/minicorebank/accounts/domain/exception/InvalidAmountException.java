package com.sergiom.minicorebank.accounts.domain.exception;

/**
 * Se lanza cuando un importe de dinero es inválido
 * (por ejemplo, menor o igual que cero).
 */
public class InvalidAmountException extends RuntimeException {
    public InvalidAmountException(long amountMinor) {
        super("amount must be > 0: " + amountMinor);
    }
}
