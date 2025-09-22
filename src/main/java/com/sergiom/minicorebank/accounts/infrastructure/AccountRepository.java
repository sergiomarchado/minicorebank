package com.sergiom.minicorebank.accounts.infrastructure;

import com.sergiom.minicorebank.accounts.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio de acceso a datos para {@link Account}.
 * <p>
 * ¿Qué es un repositorio?
 * - Es una interfaz que Spring Data JPA implementa automáticamente en tiempo de ejecución.
 * - Contiene las operaciones CRUD básicas (crear, leer, actualizar, borrar) gracias a
 *   extender {@link JpaRepository}.
 * - Te evita escribir código SQL/JPQL repetitivo.
 * <p>
 * Tipo de clave:
 * - La entidad {@link Account} usa {@link UUID} como id, por eso la firma es JpaRepository<Account, UUID>.
 * <p>
 * Métodos heredados:
 * - save(account) → guarda o actualiza una cuenta.
 * - findById(uuid) → busca por id.
 * - findAll() → devuelve todas las cuentas.
 * - deleteById(uuid) → borra por id.
 * <p>
 * Método custom:
 * - findByIban(String iban): busca una cuenta por su IBAN único.
 *   - Devuelve Optional<Account> para manejar el caso de “no encontrada”.
 * <p>
 * Ejemplos de uso:
 * {@code
 *   Optional<Account> acc = accountRepository.findByIban("ES7620770024003102575766");
 *   acc.ifPresent(a -> System.out.println(a.getCurrency()));
 * }
 *
 * Nota:
 * - Evita exponer entidades directamente en la API → mejor convertir a DTOs.
 */
public interface AccountRepository extends JpaRepository<Account, UUID> {

    /**
     * Busca una cuenta por su IBAN.
     *
     * @param iban IBAN único de la cuenta (ej. "ES7620770024003102575766").
     * @return Optional vacío si no existe; Optional<Account> con la entidad si la encuentra.
     */
    Optional<Account> findByIban(String iban);
}
