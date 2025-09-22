package com.sergiom.minicorebank.accounts.api.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Petición para registrar un depósito en una cuenta.
 * <p>
 * ¿Por qué "amountMinor"?
 * - Trabajamos con la unidad más pequeña de la moneda (céntimos para EUR)
 *   para evitar errores con decimales. Ej.: 10,23 € = 1023.
 * <p>
 * Validaciones:
 * - amountMinor > 0 (no se permiten importes cero o negativos).
 * - description es opcional; si viene, se limita en longitud para mantener datos limpios.
 * <p>
 * Ejemplo JSON:
 * {
 *   "amountMinor": 1000,
 *   "description": "Ingreso inicial"
 * }
 */
@Schema(name = "DepositRequest", description = "Datos para registrar un depósito (en unidades menores)")
public record DepositRequest(

        @Schema(
                description = "Importe del depósito en unidades menores (p. ej., céntimos)",
                example = "1000",
                minimum = "1",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @Positive(message = "amountMinor must be > 0")
        long amountMinor,

        @Schema(
                description = "Descripción opcional del depósito (máx. 140 caracteres)",
                example = "Ingreso inicial",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        @Size(max = 140, message = "description too long (max 140)")
        String description
) {}
