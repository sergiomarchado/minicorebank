package com.sergiom.minicorebank.accounts.api;

import com.sergiom.minicorebank.accounts.application.AccountService;
import com.sergiom.minicorebank.accounts.api.dtos.AccountResponse;
import com.sergiom.minicorebank.accounts.api.dtos.BalanceResponse;
import com.sergiom.minicorebank.accounts.api.dtos.CreateAccountRequest;
import com.sergiom.minicorebank.accounts.api.dtos.DepositRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.UUID;

/**
 * Controlador REST para la gestión de cuentas.
 * <p>
 * ¿Qué es un controlador?
 * - Es la “puerta de entrada” de la API. Recibe peticiones HTTP,
 *   valida la entrada y delega la lógica al servicio.
 * - Devuelve DTOs (objetos simples) como respuesta JSON.
 * <p>
 * Ruta base:
 * - Todas las rutas empiezan con /api/v1/accounts (versión 1 de la API de cuentas).
 * <p>
 * Convenciones:
 * - @RestController → marca la clase como controlador REST.
 * - @RequestMapping → define la ruta base común.
 * - @Valid → activa validaciones (Bean Validation) sobre los DTOs de entrada.
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

    /**
     * Crea una nueva cuenta bancaria para un cliente.
     * <p>
     * Método: POST
     * URL: /api/v1/accounts
     * <p>
     * Ejemplo de petición (JSON):
     * {
     *   "customerId": "9a57e640-4737-4e8a-b4c5-3ab1bfe6c5a1",
     *   "currency": "EUR"
     * }
     * <p>
     * Respuesta (201 Created, JSON):
     * {
     *   "id": "b2f7dcd3-1b9b-4f9b-a9b4-87e8c3f9d6ac",
     *   "iban": "ES7620770024003102575766",
     *   "currency": "EUR",
     *   "status": "ACTIVE"
     * }
     *
     * @param req datos de creación de cuenta (validados automáticamente)
     * @return datos básicos de la cuenta creada
     */
    @Operation(summary = "Crear cuenta", description = "Crea una cuenta (IBAN ES ficticio) para un cliente existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cuenta creada"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    @ResponseStatus(HttpStatus.CREATED) // 201: creado correctamente
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public AccountResponse create(@RequestBody @Valid CreateAccountRequest req) {
        return service.create(req);
    }

    /**
     * Consulta el saldo de una cuenta existente.
     * <p>
     * Método: GET
     * URL: /api/v1/accounts/{id}/balance
     * <p>
     * Ejemplo:
     * GET /api/v1/accounts/b2f7dcd3-1b9b-4f9b-a9b4-87e8c3f9d6ac/balance
     * <p>
     * Respuesta (200 OK):
     * {
     *   "balanceMinor": 5000
     * }
     *
     * @param id identificador único de la cuenta (UUID)
     * @return saldo actual en unidades menores (ej: céntimos)
     */
    @Operation(summary = "Saldo de cuenta", description = "Devuelve el saldo actual de la cuenta en unidades menores (p. ej., céntimos).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Saldo devuelto"),
            @ApiResponse(responseCode = "404", description = "Cuenta no encontrada")
    })
    @GetMapping("/{id}/balance")
    public BalanceResponse balance(@PathVariable UUID id) {
        return service.balance(id);
    }

    /**
     * Registra un depósito en la cuenta indicada.
     * <p>
     * Método: POST
     * URL: /api/v1/accounts/{id}/deposit
     * <p>
     * Ejemplo de petición:
     * {
     *   "amountMinor": 1000,
     *   "description": "Ingreso inicial"
     * }
     * <p>
     * Respuesta (200 OK):
     * {
     *   "balanceMinor": 6000
     * }
     * <p>
     * Validaciones:
     * - amountMinor > 0 (validado en servicio y/o DTO).
     * - La cuenta debe existir.
     *
     * @param id id de la cuenta
     * @param req datos del depósito (monto y descripción opcional)
     * @return saldo actualizado tras el depósito
     */
    @Operation(summary = "Depósito en cuenta", description = "Registra un ingreso (CREDIT) y devuelve el saldo resultante.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Depósito aplicado"),
            @ApiResponse(responseCode = "400", description = "Importe inválido"),
            @ApiResponse(responseCode = "404", description = "Cuenta no encontrada")
    })
    @PostMapping(value = "/{id}/deposit", consumes = MediaType.APPLICATION_JSON_VALUE)
    public BalanceResponse deposit(@PathVariable UUID id,
                                   @RequestBody @Valid DepositRequest req) {
        return service.deposit(id, req.amountMinor(), req.description());
    }
}
