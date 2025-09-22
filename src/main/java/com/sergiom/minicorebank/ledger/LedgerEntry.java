package com.sergiom.minicorebank.ledger;

import com.sergiom.minicorebank.accounts.domain.Account;
import com.sergiom.minicorebank.common.CurrencyCode;
import com.sergiom.minicorebank.ledger.domain.EntryDirection;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Movimiento contable (“apunte”) asociado a una cuenta.
 * Qué modela:
 * - Una operación de dinero en la cuenta con una dirección (DEBIT o CREDIT).
 * - Un importe en unidades menores (céntimos para EUR).
 * - La moneda, una descripción y un id de transacción (txnId) para idempotencia.
 * Reglas importantes:
 * - amountMinor > 0 (el signo lo aporta la dirección).
 * - currency debe coincidir con la moneda de la cuenta.
 * - txnId debe ser único para evitar duplicados si el cliente reintenta la misma petición.
 * Cómo se usa:
 * - CREDIT aumenta el saldo; DEBIT lo disminuye.
 * - El saldo se calcula sumando +amount para CREDIT y -amount para DEBIT.
 */
@Entity
@Table(
        name = "ledger_entries",
        indexes = {
                @Index(name = "idx_ledger_entries_account_id", columnList = "account_id"),
                @Index(name = "idx_ledger_entries_created_at", columnList = "created_at")
        },
        uniqueConstraints = {
                // Idempotencia: mismo txn_id no se puede registrar dos veces.
                @UniqueConstraint(name = "uk_ledger_entries_txn_id", columnNames = { "txn_id" })
                // Alternativa por cuenta (si compartes txnId entre servicios):
                // @UniqueConstraint(name = "uk_ledger_entries_account_txn", columnNames = { "account_id", "txn_id" })
        }
)
public class LedgerEntry {

    /** Identificador del apunte (UUID). */
    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    /**
     * Cuenta a la que pertenece el apunte.
     * LAZY para no cargar la cuenta completa si no es necesario.
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    /**
     * Identificador de la transacción de negocio.
     * Sirve para idempotencia: la misma transacción no se registra dos veces.
     */
    @Column(name = "txn_id", nullable = false, updatable = false)
    private UUID txnId;

    /**
     * Dirección del movimiento:
     * - CREDIT: entra dinero (suma).
     * - DEBIT : sale dinero (resta).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 6)
    private EntryDirection direction;

    /**
     * Importe en unidades menores (p. ej. céntimos). Siempre positivo.
     * El signo lo determina {@link #direction}.
     */
    @Column(name = "amount_minor", nullable = false)
    private long amountMinor;

    /** Moneda del apunte (debe coincidir con la de la cuenta). */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private CurrencyCode currency;

    /** Texto descriptivo opcional (máx. 255). */
    @Column(length = 255)
    private String description;

    /** Marca de tiempo de creación. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ------------------------------------------------------------
    // Constructores
    // ------------------------------------------------------------

    /** Requerido por JPA. */
    public LedgerEntry() {}

    /**
     * Constructor de conveniencia para crear un apunte.
     * No establece id/createdAt (lo hace {@link #prePersist()}).
     */
    public LedgerEntry(Account account,
                       UUID txnId,
                       EntryDirection direction,
                       long amountMinor,
                       CurrencyCode currency,
                       String description) {
        this.account = account;
        this.txnId = txnId;
        this.direction = direction;
        this.amountMinor = amountMinor;
        this.currency = currency;
        this.description = description;
    }

    // ------------------------------------------------------------
    // Callbacks JPA
    // ------------------------------------------------------------

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
        createdAt = LocalDateTime.now();
    }

    // ------------------------------------------------------------
    // Getters/Setters
    // ------------------------------------------------------------

    public UUID getId() { return id; }

    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }

    public UUID getTxnId() { return txnId; }
    public void setTxnId(UUID txnId) { this.txnId = txnId; }

    public EntryDirection getDirection() { return direction; }
    public void setDirection(EntryDirection direction) { this.direction = direction; }

    public long getAmountMinor() { return amountMinor; }
    public void setAmountMinor(long amountMinor) { this.amountMinor = amountMinor; }

    public CurrencyCode getCurrency() { return currency; }
    public void setCurrency(CurrencyCode currency) { this.currency = currency; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    // ------------------------------------------------------------
    // Utilidad: igualdad y logging
    // ------------------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LedgerEntry that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return (id != null) ? id.hashCode() : Objects.hash(getClass());
    }

    @Override
    public String toString() {
        return "LedgerEntry{id=%s, account=%s, dir=%s, amountMinor=%d, currency=%s}"
                .formatted(id, account != null ? account.getId() : null, direction, amountMinor, currency);
    }

}
