package com.sergiom.minicorebank.customers.application;

import com.sergiom.minicorebank.customers.api.dto.CreateCustomerRequest;
import com.sergiom.minicorebank.customers.api.dto.CustomerResponse;
import com.sergiom.minicorebank.customers.domain.Customer;
import com.sergiom.minicorebank.customers.domain.exception.CustomerNotFoundException;
import com.sergiom.minicorebank.customers.domain.exception.EmailAlreadyInUseException;
import com.sergiom.minicorebank.customers.infrastructure.CustomerRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

/**
 * Casos de uso de clientes: alta y consulta.
 * - "Servicio" = capa de aplicación. Aquí va la lógica de negocio y orquestación
 *   (validaciones, llamadas a repositorios, construcción de DTOs, etc.).
 * - Las entidades (Customer) representan el modelo persistente/JPA.
 * - Los DTOs (CreateCustomerRequest, CustomerResponse) son clases simples para la API.
 * Reglas:
 * - Antes de crear, comprobar que el email no esté en uso → si lo está, 409 Conflict.
 * - Además, la BD tiene UNIQUE(email) por si hay condiciones de carrera.
 *   Si salta DataIntegrityViolationException, también devolvemos 409.
 * - Normalizamos el email a minúsculas para evitar duplicados por casing.
 */
@Service
public class CustomerService {

    private final CustomerRepository customers;

    public CustomerService(CustomerRepository customers) {
        this.customers = customers;
    }

    /**
     * Da de alta un cliente nuevo.
     * - Devuelve 409 (EmailAlreadyInUseException) si el email ya existe.
     * - Normaliza el email a minúsculas antes de guardar/consultar.
     */
    @Transactional
    public CustomerResponse create(CreateCustomerRequest req) {
        // Normalización básica: trim y email en minúsculas (evita duplicados Ada@ vs ada@)
        final String fullName = req.fullName().trim();
        final String email = req.email().trim().toLowerCase(Locale.ROOT);

        // Comprobación previa (respuesta más rápida y mensaje claro)
        if (customers.existsByEmail(email)) {
            throw new EmailAlreadyInUseException(email); // nuestro 409 predecible
        }

        try {
            Customer c = new Customer(fullName, email);
            c = customers.save(c);
            return CustomerResponse.fromEntity(c);
        } catch (DataIntegrityViolationException dive) {
            // Seguridad frente a carreras (dos peticiones a la vez con el mismo email)
            throw new EmailAlreadyInUseException(email);
        }
    }

    /**
     * Busca un cliente por id.
     * - Devuelve 404 (CustomerNotFoundException) si no existe.
     */
    @Transactional(readOnly = true)
    public CustomerResponse get(UUID id) {
        Customer c = customers.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
        return CustomerResponse.fromEntity(c);
    }
}
