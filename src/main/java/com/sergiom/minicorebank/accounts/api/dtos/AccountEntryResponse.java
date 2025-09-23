package com.sergiom.minicorebank.accounts.api.dtos;

import com.sergiom.minicorebank.common.CurrencyCode;
import com.sergiom.minicorebank.ledger.domain.EntryDirection;
import com.sergiom.minicorebank.ledger.LedgerEntry;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de SALIDA para exponer movimientos recientes de una cuenta.
 * Por qué un DTO:
 * - No exponemos la entidad JPA. Controlamos exactamente qué campos devolvemos.
 * - Es estable hacia fuera y fácil de documentar en OpenAPI.
 */
@Schema(name = "AccountEntryResponse", description = "Apunte contable de la cuenta (ledger entry)")
public record AccountEntryResponse(

        @Schema(description = "Identificador de transacción (idempotencia)", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID txnId,

        @Schema(description = "Dirección del apunte (CREDIT suma, DEBIT resta)", example = "CREDIT")
        EntryDirection direction,

        @Schema(description = "Importe en unidades menores (p. ej. céntimos)", example = "1500")
        long amountMinor,

        @Schema(description = "Moneda ISO-4217", example = "EUR")
        CurrencyCode currency,

        @Schema(description = "Descripción del apunte", example = "Ingreso inicial")
        String description,

        @Schema(description = "Fecha de creación del apunte (servidor)", example = "2025-09-23T10:15:30")
        LocalDateTime createdAt
) {
    /** Mapeo cómodo desde la entidad. */
    public static AccountEntryResponse fromEntity(LedgerEntry e) {
        return new AccountEntryResponse(
                e.getTxnId(),
                e.getDirection(),
                e.getAmountMinor(),
                e.getCurrency(),
                e.getDescription(),
                e.getCreatedAt()
        );
    }
}
