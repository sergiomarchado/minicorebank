package com.sergiom.minicorebank.accounts.api.dtos;

import com.sergiom.minicorebank.accounts.domain.Account;
import com.sergiom.minicorebank.accounts.domain.AccountStatus;
import com.sergiom.minicorebank.common.CurrencyCode;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

/**
 * Respuesta “plana” que representa una cuenta para exponer por la API.
 * <p>
 * ¿Qué es un DTO?
 * - Un objeto pensado para ENTRAR o SALIR por la API.
 * - No es la entidad JPA. Es más seguro y estable: controlas qué campos muestras.
 * <p>
 * ¿Por qué usar record?
 * - Inmutable (no tiene setters) → más simple y seguro.
 * - Genera automáticamente constructor y getters.
 * <p>
 * Ejemplo JSON de respuesta:
 * {
 *   "id": "b2f7dcd3-1b9b-4f9b-a9b4-87e8c3f9d6ac",
 *   "iban": "ES7620770024003102575766",
 *   "currency": "EUR",
 *   "status": "ACTIVE"
 * }
 */
@Schema(name = "AccountResponse", description = "Datos públicos de una cuenta")
public record AccountResponse(

        @Schema(
                description = "Identificador único de la cuenta (UUID)",
                example = "b2f7dcd3-1b9b-4f9b-a9b4-87e8c3f9d6ac"
        )
        UUID id,

        @Schema(
                description = "IBAN único de la cuenta",
                example = "ES7620770024003102575766"
        )
        String iban,

        @Schema(
                description = "Moneda de la cuenta (ISO-4217)",
                example = "EUR"
        )
        CurrencyCode currency,

        @Schema(
                description = "Estado actual de la cuenta",
                example = "ACTIVE"
        )
        AccountStatus status
) {
    /**
     * Crea un AccountResponse a partir de la entidad Account.
     * Útil en servicios/controladores para no repetir mapeo campo a campo.
     */
    public static AccountResponse fromEntity(Account a) {
        return new AccountResponse(a.getId(), a.getIban(), a.getCurrency(), a.getStatus());
    }
}
