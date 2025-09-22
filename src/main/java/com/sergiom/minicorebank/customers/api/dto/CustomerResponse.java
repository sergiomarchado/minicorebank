package com.sergiom.minicorebank.customers.api.dto;

import com.sergiom.minicorebank.customers.domain.Customer;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

/**
 * DTO de SALIDA con los datos públicos del cliente.
 */
@Schema(name = "CustomerResponse", description = "Datos públicos de un cliente")
public record CustomerResponse(
        @Schema(description = "Identificador (UUID)", example = "7b6f0b9e-8db3-49b0-a9d8-6c9d1a3e2d10")
        UUID id,
        @Schema(description = "Nombre", example = "Ada Lovelace")
        String name,
        @Schema(description = "Email", example = "ada@example.com")
        String email
) {
    /** Mapeo cómodo desde la entidad. */
    public static CustomerResponse fromEntity(Customer c) {
        return new CustomerResponse(c.getId(), c.getName(), c.getEmail());
    }
}
