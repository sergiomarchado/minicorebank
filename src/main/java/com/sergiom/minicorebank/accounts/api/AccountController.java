package com.sergiom.minicorebank.accounts.api;

import com.sergiom.minicorebank.accounts.application.AccountService;
import com.sergiom.minicorebank.accounts.api.dtos.AccountEntryResponse;
import com.sergiom.minicorebank.accounts.api.dtos.AccountResponse;
import com.sergiom.minicorebank.accounts.api.dtos.BalanceResponse;
import com.sergiom.minicorebank.accounts.api.dtos.CreateAccountRequest;
import com.sergiom.minicorebank.accounts.api.dtos.DepositRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.UUID;

/**
 * Controlador REST para la gestión de cuentas.
 */
@Tag(name = "account-controller", description = "Operaciones con cuentas bancarias")
@RestController
@RequestMapping(
        value = "/api/v1/accounts",
        produces = MediaType.APPLICATION_JSON_VALUE
)
public class AccountController {

    private final AccountService service;

    public AccountController(AccountService service) {
        this.service = service;
    }

    // --- Crear cuenta ---
    @Operation(summary = "Crear cuenta", description = "Crea una cuenta (IBAN fake) para un cliente existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cuenta creada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public AccountResponse create(@RequestBody @Valid CreateAccountRequest req) {
        return service.create(req);
    }

    // --- Saldo ---
    @Operation(summary = "Saldo de cuenta", description = "Devuelve el saldo actual de la cuenta en unidades menores (p. ej., céntimos).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Saldo devuelto"),
            @ApiResponse(responseCode = "404", description = "Cuenta no encontrada")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{id}/balance")
    public BalanceResponse balance(@PathVariable UUID id) {
        return service.balance(id);
    }

    // --- Depósito ---
    @Operation(summary = "Depósito en cuenta", description = "Registra un ingreso (CREDIT) y devuelve el saldo resultante.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Depósito aplicado"),
            @ApiResponse(responseCode = "400", description = "Importe inválido"),
            @ApiResponse(responseCode = "404", description = "Cuenta no encontrada")
    })
    @ResponseStatus(HttpStatus.OK)
    @PostMapping(value = "/{id}/deposit", consumes = MediaType.APPLICATION_JSON_VALUE)
    public BalanceResponse deposit(@PathVariable UUID id,
                                   @RequestBody @Valid DepositRequest req) {
        return service.deposit(id, req.amountMinor(), req.description());
    }

    /**
     * Movimientos recientes de una cuenta (máximo 50).
     * <p>
     * Método: GET
     * URL: /api/v1/accounts/{id}/entries?size=50
     * <p>
     * Respuesta (200 OK): lista de apuntes ordenados por fecha desc.
     *
     * @param id id de la cuenta
     * @param size número de elementos (1..50), por defecto 50
     */
    @Operation(summary = "Movimientos recientes", description = "Devuelve los últimos N apuntes del ledger para la cuenta.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado devuelto"),
            @ApiResponse(responseCode = "404", description = "Cuenta no encontrada")
    })
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{id}/entries")
    public List<AccountEntryResponse> recentEntries(
            @PathVariable UUID id,
            @Parameter(description = "Número de elementos a devolver (1..50). Por defecto 50.")
            @RequestParam(name = "size", defaultValue = "50")
            @Min(1) @Max(50) int size
    ) {
        return service.recentEntries(id, size);
    }
}
