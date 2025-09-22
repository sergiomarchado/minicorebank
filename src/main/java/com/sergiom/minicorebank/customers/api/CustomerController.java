package com.sergiom.minicorebank.customers.api;

import com.sergiom.minicorebank.customers.api.dto.CreateCustomerRequest;
import com.sergiom.minicorebank.customers.api.dto.CustomerResponse;
import com.sergiom.minicorebank.customers.application.CustomerService;
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
 * Endpoints REST del contexto "customers".
 * <p>
 * Ruta base: /api/v1/customers
 * <p>
 * ¿Qué hace este controlador?
 * - Expone la API para dar de alta y consultar clientes.
 * - Valida la entrada (@Valid) y delega la lógica al servicio.
 * - Devuelve DTOs simples en JSON.
 */
@Tag(name = "customer-controller", description = "Operaciones con clientes")
@RestController
@RequestMapping(
        value = "/api/v1/customers",
        produces = MediaType.APPLICATION_JSON_VALUE
)
public class CustomerController {

    private final CustomerService service;

    public CustomerController(CustomerService service) {
        this.service = service;
    }

    /**
     * Alta de cliente.
     * <p>
     * Método: POST
     * URL: /api/v1/customers
     * <p>
     * Ejemplo de petición:
     * {
     *   "fullName": "Ada Lovelace",
     *   "email": "ada@example.com"
     * }
     * <p>
     * Respuesta (201 Created):
     * {
     *   "id": "6e0f0a62-0f5e-45e0-9a5a-7b6e2b9b1a01",
     *   "fullName": "Harry Potter",
     *   "email": "harry@example.com"
     * }
     */
    @Operation(summary = "Alta de cliente", description = "Crea un nuevo cliente si el email no existe.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cliente creado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "409", description = "Email ya registrado")
    })
    @ResponseStatus(HttpStatus.CREATED) // devolver 201 en creación
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public CustomerResponse create(@RequestBody @Valid CreateCustomerRequest req) {
        return service.create(req);
    }

    /**
     * Detalle de cliente por id.
     * <p>
     * Método: GET
     * URL: /api/v1/customers/{id}
     * <p>
     * Respuesta (200 OK):
     * {
     *   "id": "6e0f0a62-0f5e-45e0-9a5a-7b6e2b9b1a01",
     *   "fullName": "Harry Potter",
     *   "email": "harry@example.com"
     * }
     */
    @Operation(summary = "Detalle de cliente", description = "Devuelve un cliente por su id (UUID).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    @GetMapping("/{id}")
    public CustomerResponse get(@PathVariable UUID id) {
        return service.get(id);
    }
}
