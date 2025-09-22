package com.sergiom.minicorebank.ledger.infrastructure;

import com.sergiom.minicorebank.ledger.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio de acceso a datos para {@link LedgerEntry}.
 * ¿Qué hace?
 * - Spring Data JPA genera la implementación en tiempo de ejecución.
 * - Proporciona CRUD básico y consultas personalizadas.
 * Conceptos clave:
 * - El saldo de una cuenta no está en {@code Account}; se calcula sumando apuntes:
 *   CREDIT suma, DEBIT resta.
 * - Idempotencia: cada apunte tiene un {@code txnId} único. Con {@code existsByTxnId} evitamos duplicados.
 */
public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {

    /**
     * Calcula el saldo actual de una cuenta (en unidades menores).
     * Lógica: CREDIT (+) y DEBIT (-). Si no hay apuntes, devuelve 0.
     * Nota: Como {@code direction} se persiste como texto (EnumType.STRING),
     * aquí comparamos con sus valores "CREDIT"/"DEBIT".
     * (Si extraes el enum a tipo de nivel superior, también podrías usar
     *  la constante de enum en JPQL).
     */
    @Query("""
        select coalesce(
          sum(case
                when e.direction = 'CREDIT' then e.amountMinor
                when e.direction = 'DEBIT'  then -e.amountMinor
              end), 0)
        from LedgerEntry e
        where e.account.id = :accountId
    """)
    long balanceOf(@Param("accountId") UUID accountId);

    /**
     * ¿Existe ya un apunte con este identificador de transacción?
     * Útil para implementar idempotencia en depósitos/retiradas.
     * (Refuerza esto con un UNIQUE en BD sobre txn_id).
     */
    boolean existsByTxnId(UUID txnId);

    /**
     * Últimos movimientos de una cuenta (máx. 50), ordenados por fecha descendente.
     * Útil para mostrar “actividad reciente”.
     */
    List<LedgerEntry> findTop50ByAccount_IdOrderByCreatedAtDesc(UUID accountId);
}
