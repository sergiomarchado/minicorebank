package com.sergiom.minicorebank.accounts.api.dtos;

import com.sergiom.minicorebank.common.CurrencyCode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Petición para crear una nueva cuenta bancaria.
 * <p>
 * ¿Qué es esto?
 * - Es un DTO de ENTRADA: representa el JSON que el cliente envía al API.
 * - No es la entidad Account: así controlamos qué datos se pueden dar de alta desde fuera.
 * <p>
 * Validaciones:
 * - customerId: obligatorio → el cliente debe existir en la base de datos.
 * - currency: obligatorio → la cuenta debe tener moneda definida.
 * <p>
 * Ejemplo JSON de entrada:
 * {
 *   "customerId": "9a57e640-4737-4e8a-b4c5-3ab1bfe6c5a1",
 *   "currency": "EUR"
 * }
 */
@Schema(name = "CreateAccountRequest", description = "Datos necesarios para crear una cuenta")
public record CreateAccountRequest(

        @Schema(
                description = "Identificador del cliente dueño de la cuenta (UUID)",
                example = "9a57e640-4737-4e8a-b4c5-3ab1bfe6c5a1",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull
        UUID customerId,

        @Schema(
                description = "Moneda de la cuenta (ISO-4217). Debe ser uno de los valores soportados.",
                example = "EUR",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull
        CurrencyCode currency
) {}
