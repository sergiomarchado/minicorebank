package com.sergiom.minicorebank.accounts.domain;

/**
 * Estados posibles de una cuenta.
 * - ACTIVE: cuenta operativa.
 * - BLOCKED: operaciones temporalmente bloqueadas (fraude, impago, etc.).
 * - CLOSED: cuenta cerrada; no debe permitir nuevos movimientos.
 */
public enum AccountStatus {
    ACTIVE, BLOCKED, CLOSED
}