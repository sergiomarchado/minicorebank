package com.sergiom.minicorebank.accounts.domain;

import com.sergiom.minicorebank.common.CurrencyCode;
import com.sergiom.minicorebank.customers.domain.Customer;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad JPA que representa una cuenta bancaria.
 * <p>
 * Qué modela:
 * - El dueño de la cuenta (customer).
 * - El IBAN único que identifica la cuenta.
 * - La moneda (currency) y el estado (status).
 * - Fechas de creación/actualización y control de concurrencia (version).
 * <p>
 * Importante:
 * - Esta entidad NO guarda el saldo. En un core bancario el saldo se calcula con el "libro mayor"
 *   (ledger) sumando movimientos. Así mantenemos trazabilidad contable.
 * <p>
 * Cómo se usa:
 * - La capa de servicio crea/modifica cuentas y valida reglas de negocio.
 * - La API REST nunca debería exponer la entidad tal cual; usa DTOs.
 */
@Entity
@Table(
        name = "accounts",
        // Índices para consultas rápidas y unicidad de IBAN
        indexes = {
                @Index(name = "idx_accounts_customer_id", columnList = "customer_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_accounts_iban", columnNames = {"iban"})
        }
)
public class Account {

    /**
     * Identificador único global de la cuenta.
     * - Usamos UUID para no depender de secuencias/auto-incrementos.
     * - Se genera en {@link #prePersist()} si viene nulo.
     */
    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    /**
     * Dueño de la cuenta.
     * <p>
     * Por qué LAZY:
     * - Para no cargar el Customer completo si solo necesitamos datos de Account.
     * - Evita problemas de rendimiento (consultas N+1).
     * Nota: Al serializar a JSON la entidad, no escales relaciones LAZY (usa DTOs).
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    /**
     * IBAN de la cuenta.
     * - Debe ser único (constraint a nivel de tabla).
     * - La validación de formato se hace en la capa de servicio/validador.
     */
    @Column(nullable = false, unique = true, length = 34)
    private String iban;

    /**
     * Moneda de la cuenta, guardada como texto (por ejemplo "EUR").
     * - Las cuentas son mono-divisa: no se mezclan monedas en la misma cuenta.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private CurrencyCode currency;

    /**
     * Estado de la cuenta (ej.: ACTIVA, BLOQUEADA, CERRADA).
     * - Usamos enum para evitar valores no válidos.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountStatus status = AccountStatus.ACTIVE;

    /**
     * Control de concurrencia optimista.
     * - Cuando dos peticiones intentan guardar cambios a la vez,
     *   si la versión no coincide, Hibernate lanza OptimisticLockException.
     */
    @Version
    private Integer version;

    /** Fecha/hora de creación de la fila. Se rellena al insertar. */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Fecha/hora de la última actualización. Se actualiza en cada cambio. */
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // ------------------------------------------------------------
    // Constructores
    // ------------------------------------------------------------

    /** Constructor sin argumentos requerido por JPA. */
    protected Account() {
    }

    /**
     * Constructor de conveniencia para crear una cuenta desde la capa de servicio.
     * No establece id/fechas; eso lo hacen los callbacks JPA.
     */
    public Account(Customer customer, String iban, CurrencyCode currency) {
        this.customer = customer;
        this.iban = iban;
        this.currency = currency;
        this.status = AccountStatus.ACTIVE;
    }

    // ------------------------------------------------------------
    // Callbacks JPA (se ejecutan automáticamente en persist/merge)
    // ------------------------------------------------------------

    @PrePersist
    void prePersist() {
        // Generamos id si no viene de fuera (tests, importaciones, etc.).
        if (id == null) {
            id = UUID.randomUUID();
        }

        // Timestamps iniciales (ahora). En creación, ambos son iguales.
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;

        // La versión inicial es 0 si el proveedor JPA no la pone automáticamente.
        if (version == null) {
            version = 0;
        }
    }

    @PreUpdate
    void preUpdate() {
        // En cada modificación refrescamos la marca de actualización.
        updatedAt = LocalDateTime.now();
    }

    // ------------------------------------------------------------
    // Getters/Setters
    // ------------------------------------------------------------

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public String getIban() { return iban; }
    public void setIban(String iban) { this.iban = iban; }

    public CurrencyCode getCurrency() { return currency; }
    public void setCurrency(CurrencyCode currency) { this.currency = currency; }

    public AccountStatus getStatus() { return status; }
    public void setStatus(AccountStatus status) { this.status = status; }

    public Integer getVersion() { return version; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // ------------------------------------------------------------
    // Igualdad y hashing
    // ------------------------------------------------------------

    /**
     * Dos cuentas son iguales si comparten el mismo id no nulo.
     * Antes de persistir (id nulo) no deben considerarse iguales.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Account that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        // Si id es nulo (entidad transitoria), usa hash de la clase para evitar colisiones inestables.
        return (id != null) ? id.hashCode() : Objects.hash(getClass());
    }

    @Override
    public String toString() {
        return "Account{id=%s, iban=%s, currency=%s, status=%s}".formatted(
                id, iban, currency, status
        );
    }

}
