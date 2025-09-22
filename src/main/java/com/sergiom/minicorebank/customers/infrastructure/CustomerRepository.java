package com.sergiom.minicorebank.customers.infrastructure;

import com.sergiom.minicorebank.customers.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio JPA de clientes.
 * Métodos útiles:
 * - findByEmail: buscar por email.
 * - existsByEmail: comprobar existencia (para 409 predecible).
 */
public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    Optional<Customer> findByEmail(String email);

    boolean existsByEmail(String email);
}
