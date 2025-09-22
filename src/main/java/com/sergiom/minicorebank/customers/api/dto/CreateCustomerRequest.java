package com.sergiom.minicorebank.customers.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de ENTRADA para crear cliente.
 *
 * Validaciones:
 * - fullName obligatorio (1..120, NotBlank ya evita vacío/solo espacios).
 * - email obligatorio y con formato de email.
 */
@Schema(name = "CreateCustomerRequest", description = "Datos para dar de alta a un cliente")
public record CreateCustomerRequest(

        @Schema(
                description = "Nombre completo del cliente",
                example = "Ada Lovelace",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank
        @Size(max = 120)
        String fullName,

        @Schema(
                description = "Email único del cliente",
                example = "ada@example.com",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank
        @Email
        @Size(max = 255)
        String email
) {}
