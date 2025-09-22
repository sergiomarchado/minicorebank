package com.sergiom.minicorebank.accounts.api.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Respuesta con el saldo actual de una cuenta.
 * <p>
 * ¿Por qué "minor units"?
 * - Para evitar errores con decimales (ej.: 10,23 €), en banca se trabaja en la unidad más pequeña
 *   de la moneda: céntimos para EUR, peniques para GBP, etc.
 * - Así, 10,23 € se representa como 1023 (long).
 * <p>
 * ¿Por qué long?
 * - El saldo puede crecer; long evita desbordarse antes que int.
 * - No usamos BigDecimal aquí porque NO formateamos dinero, sólo lo transportamos.
 * <p>
 * Ejemplo JSON:
 * { "balanceMinor": 152375 }  // 1.523,75 € si la moneda es EUR
 */
@Schema(name = "BalanceResponse", description = "Saldo actual en unidades menores (céntimos para EUR)")
public record BalanceResponse(

        @Schema(
                description = "Saldo en unidades menores (ej.: céntimos para EUR)",
                example = "152375"
        )
        long balanceMinor
) {
    /**
     * Crea una respuesta con saldo cero.
     * Útil para inicializar respuestas sin nulls.
     */
    public static BalanceResponse zero() {
        return new BalanceResponse(0L);
    }
}
