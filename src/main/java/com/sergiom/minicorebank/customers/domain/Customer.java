package com.sergiom.minicorebank.customers.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad JPA que representa a un cliente del banco.
 *
 * Qué modela:
 * - Datos básicos: nombre y email.
 * - email debe ser ÚNICO (constraint en BD) para evitar duplicados.
 *
 * Diseño:
 * - La validación de formato (ej. email) se hace en los DTOs de entrada.
 * - La API no expone la entidad directamente; usamos DTOs.
 */
@Entity
@Table(
        name = "customers",
        indexes = {
                @Index(name = "idx_customers_email", columnList = "email")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_customers_email", columnNames = {"email"})
        }
)
public class Customer {

    /** Identificador único del cliente. */
    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    /** Nombre visible del cliente. */
    @Column(nullable = false, length = 120)
    private String name;

    /** Email único: lo usamos como dato de contacto y para evitar duplicados. */
    @Column(nullable = false, length = 255, unique = true)
    private String email;

    /** Fechas de auditoría. */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // --------- Constructores ---------

    protected Customer() {}

    public Customer(String name, String email) {
        this.name = name;
        this.email = email;
    }

    // --------- Callbacks JPA ---------

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // --------- Getters/Setters ---------

    public UUID getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // --------- Igualdad/logging ---------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Customer that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return (id != null) ? id.hashCode() : Objects.hash(getClass());
    }

    @Override
    public String toString() {
        return "Customer{id=%s, email=%s}".formatted(id, email);
    }
}
