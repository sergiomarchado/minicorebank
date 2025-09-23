package com.sergiom.minicorebank.accounts.application;

import com.sergiom.minicorebank.accounts.domain.Account;
import com.sergiom.minicorebank.accounts.domain.IbanGenerator;
import com.sergiom.minicorebank.accounts.domain.exception.AccountNotFoundException;
import com.sergiom.minicorebank.accounts.domain.exception.InvalidAmountException;
import com.sergiom.minicorebank.accounts.api.dtos.AccountEntryResponse;
import com.sergiom.minicorebank.accounts.api.dtos.AccountResponse;
import com.sergiom.minicorebank.accounts.api.dtos.BalanceResponse;
import com.sergiom.minicorebank.accounts.api.dtos.CreateAccountRequest;
import com.sergiom.minicorebank.accounts.infrastructure.AccountRepository;
import com.sergiom.minicorebank.customers.domain.Customer;
import com.sergiom.minicorebank.customers.domain.exception.CustomerNotFoundException;
import com.sergiom.minicorebank.customers.infrastructure.CustomerRepository;
import com.sergiom.minicorebank.ledger.LedgerEntry;
import com.sergiom.minicorebank.ledger.domain.EntryDirection;
import com.sergiom.minicorebank.ledger.infrastructure.LedgerEntryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Servicio de negocio para cuentas bancarias.
 * (ver comentarios existentes para detalle)
 */
@Service
public class AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

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

    /** Crear cuenta (ver comentarios existentes). */
    public AccountResponse create(CreateAccountRequest req) {
        // Usamos excepción de dominio coherente con el handler global (404).
        Customer c = customers.findById(req.customerId())
                .orElseThrow(() -> new CustomerNotFoundException(req.customerId()));

        Account a = new Account(c, IbanGenerator.newEsIban(), req.currency());
        a = accounts.save(a);

        return AccountResponse.fromEntity(a);
    }

    /** Saldo actual (ver comentarios existentes). */
    public BalanceResponse balance(UUID accountId) {
        if (!accounts.existsById(accountId)) {
            throw new AccountNotFoundException(accountId);
        }
        long bal = ledger.balanceOf(accountId);
        return new BalanceResponse(bal);
    }

    /**
     * Registra un depósito en la cuenta indicada (ver comentarios existentes).
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

        // Logging de auditoría (trazabilidad en demos / troubleshooting).
        log.info("deposit applied txnId={} accountId={} amountMinor={} currency={} desc={}",
                e.getTxnId(), accountId, amountMinor, a.getCurrency(), e.getDescription());

        return balance(accountId);
    }

    /**
     * Devuelve los N últimos apuntes del ledger para una cuenta (actividad reciente).
     * <p>
     * Reglas:
     * - Verificamos que la cuenta exista (404 si no).
     * - size se limita a [1..50] para evitar respuestas excesivas (y porque el repo ya trae 50).
     * - Orden: descendente por fecha de creación (más recientes primero).
     *
     * @param accountId id de la cuenta
     * @param size número de elementos a devolver (1..50)
     * @return lista de apuntes (DTO) más recientes
     */
    @Transactional(readOnly = true)
    public List<AccountEntryResponse> recentEntries(UUID accountId, int size) {
        if (!accounts.existsById(accountId)) {
            throw new AccountNotFoundException(accountId);
        }

        int capped = Math.max(1, Math.min(50, size)); // clamp [1..50]
        List<LedgerEntry> top = ledger.findTop50ByAccount_IdOrderByCreatedAtDesc(accountId);

        // Si piden menos de 50, recortamos; si piden 50, devolvemos todo.
        return top.stream()
                .limit(capped)
                .map(AccountEntryResponse::fromEntity)
                .toList();
    }
}
