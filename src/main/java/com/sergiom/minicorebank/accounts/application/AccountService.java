package com.sergiom.minicorebank.accounts.application;

import com.sergiom.minicorebank.accounts.domain.Account;
import com.sergiom.minicorebank.accounts.domain.IbanGenerator;
import com.sergiom.minicorebank.accounts.domain.exception.AccountNotFoundException;
import com.sergiom.minicorebank.accounts.domain.exception.InvalidAmountException;
import com.sergiom.minicorebank.accounts.api.dtos.AccountResponse;
import com.sergiom.minicorebank.accounts.api.dtos.BalanceResponse;
import com.sergiom.minicorebank.accounts.api.dtos.CreateAccountRequest;
import com.sergiom.minicorebank.accounts.infrastructure.AccountRepository;
import com.sergiom.minicorebank.customers.domain.Customer;
import com.sergiom.minicorebank.customers.infrastructure.CustomerRepository;
import com.sergiom.minicorebank.ledger.LedgerEntry;
import com.sergiom.minicorebank.ledger.domain.EntryDirection;
import com.sergiom.minicorebank.ledger.infrastructure.LedgerEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * Servicio de negocio para cuentas bancarias.
 * <p>
 * ¿Qué es un servicio?
 * - Una clase de la "capa de aplicación" que contiene la lógica de negocio.
 * - Orquesta llamadas a repositorios y entidades.
 * - Aplica validaciones y reglas antes de guardar datos.
 * <p>
 * Reglas principales:
 * - Solo se pueden crear cuentas para clientes existentes.
 * - El saldo no se guarda en Account; se calcula a partir del ledger.
 * - Los depósitos deben tener un monto positivo y generan una entrada contable.
 * <p>
 * Notas técnicas:
 * - Marcado con @Service para que Spring lo detecte como componente.
 * - Usa @Transactional en operaciones de escritura para garantizar atomicidad.
 */
@Service
public class AccountService {

    private final AccountRepository accounts;
    private final CustomerRepository customers;
    private final LedgerEntryRepository ledger;

    public AccountService(AccountRepository accounts,
                          CustomerRepository customers,
                          LedgerEntryRepository ledger) {
        this.accounts = accounts;
        this.customers = customers;
        this.ledger = ledger;
    }

    /**
     * Crea una nueva cuenta bancaria para un cliente existente.
     * <p>
     * Flujo:
     * 1. Busca el cliente en la base de datos.
     *    - Si no existe, lanzamos "no encontrado" (404) para el cliente.
     * 2. Construye una nueva Account con:
     *    - El cliente dueño.
     *    - La moneda indicada.
     *    - Un IBAN generado automáticamente (IbanGenerator).
     * 3. Guarda la cuenta en la base de datos.
     * 4. Devuelve un DTO con los datos visibles al exterior.
     *
     * @param req DTO con los datos de creación (id del cliente, moneda, etc.)
     * @return DTO con id, iban, moneda y estado de la nueva cuenta.
     */
    public AccountResponse create(CreateAccountRequest req) {
        Customer c = customers.findById(req.customerId())
                .orElseThrow(() -> new NoSuchElementException("customer not found")); // → 404

        Account a = new Account(c, IbanGenerator.newEsIban(), req.currency());
        a = accounts.save(a);

        // Si prefieres evitar repetir el mapping, puedes usar AccountResponse.fromEntity(a);
        return AccountResponse.fromEntity(a);
    }

    /**
     * Consulta el saldo actual de una cuenta.
     * <p>
     * Nota:
     * - El saldo no está en Account. Se calcula en tiempo real sumando todas
     *   las entradas del ledger asociadas a esa cuenta.
     * - Esto asegura trazabilidad contable (puedes reconstruir saldos históricos).
     *
     * @param accountId id de la cuenta
     * @return DTO con el saldo en unidades menores (ej: céntimos).
     */
    public BalanceResponse balance(UUID accountId) {
        // Si la cuenta no existe devolvemos un 404 claro (mapeado por el GlobalExceptionHandler).
        if (!accounts.existsById(accountId)) {
            throw new AccountNotFoundException(accountId);
        }
        long bal = ledger.balanceOf(accountId);
        return new BalanceResponse(bal);
    }

    /**
     * Registra un depósito en la cuenta indicada.
     * <p>
     * Reglas:
     * - amountMinor debe ser > 0 (expresado en unidades menores, ej: céntimos).
     * - La cuenta debe existir.
     * - Se crea una entrada en el ledger con dirección CREDIT (ingreso).
     * - Se genera un txnId único para identificar la transacción (sirve para idempotencia).
     *
     * @param accountId id de la cuenta
     * @param amountMinor monto a depositar en unidades menores (ej: céntimos)
     * @param description texto opcional (si es null se pone "deposit")
     * @return DTO con el saldo actualizado
     */
    @Transactional
    public BalanceResponse deposit(UUID accountId, long amountMinor, String description) {
        if (amountMinor <= 0) throw new InvalidAmountException(amountMinor);

        Account a = accounts.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        LedgerEntry e = new LedgerEntry();
        e.setAccount(a);
        e.setTxnId(UUID.randomUUID()); // id único de transacción (sirve para evitar duplicados en el futuro)
        e.setDirection(EntryDirection.CREDIT); // ← enum, sin strings mágicos
        e.setAmountMinor(amountMinor);
        e.setCurrency(a.getCurrency());
        e.setDescription(description != null ? description : "deposit");
        // TODO(idempotencia): añade índice único por txn_id en la tabla de ledger y/o usa existsByTxnId para evitar duplicados.
        ledger.save(e);

        return balance(accountId);
    }
}
