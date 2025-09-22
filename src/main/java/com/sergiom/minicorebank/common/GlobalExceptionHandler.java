package com.sergiom.minicorebank.common;

import com.sergiom.minicorebank.accounts.domain.exception.AccountNotFoundException;
import com.sergiom.minicorebank.accounts.domain.exception.InvalidAmountException;
import com.sergiom.minicorebank.customers.domain.exception.CustomerNotFoundException;
import com.sergiom.minicorebank.customers.domain.exception.EmailAlreadyInUseException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Manejador global de excepciones para la API REST.
 * Produce respuestas homogéneas (timestamp, path, status, error, message, details)
 * y mapea las excepciones más comunes a códigos HTTP correctos.
 */
@RestControllerAdvice(basePackages = {
        "com.sergiom.minicorebank.accounts.api",
        "com.sergiom.minicorebank.customers.api"
})
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ---------- 400 Bad Request ----------

    /** Importes inválidos, etc. (reglas de negocio). */
    @ExceptionHandler(InvalidAmountException.class)
    public ResponseEntity<?> handleInvalidAmount(InvalidAmountException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "Datos inválidos", List.of(safeMessage(ex, "importe inválido")), req);
    }

    /** Body JSON válido sintácticamente pero que no pasa Bean Validation (@Valid). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream().map(this::fmt).toList();
        return build(HttpStatus.BAD_REQUEST, "Datos inválidos", details, req);
    }

    /** Parámetros de ruta/query validados con @Validated (ConstraintViolation). */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraint(ConstraintViolationException ex, HttpServletRequest req) {
        List<String> details = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + " " + v.getMessage()).toList();
        return build(HttpStatus.BAD_REQUEST, "Parámetros inválidos", details, req);
    }

    /** Body JSON mal formado o tipos incompatibles. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleUnreadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "Cuerpo de petición inválido",
                List.of(safeMessage(ex, "JSON mal formado o tipo de dato incorrecto")), req);
    }

    /** Tipo de parámetro incompatible (p.ej., UUID inválido en path). */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<?> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        String detail = "Parámetro '%s' con tipo inválido".formatted(ex.getName());
        return build(HttpStatus.BAD_REQUEST, "Parámetro inválido", List.of(detail), req);
    }

    /** Falta un parámetro requerido en query. */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<?> handleMissingParam(MissingServletRequestParameterException ex, HttpServletRequest req) {
        String detail = "Falta parámetro requerido '%s'".formatted(ex.getParameterName());
        return build(HttpStatus.BAD_REQUEST, "Parámetro requerido ausente", List.of(detail), req);
    }

    /** Otras condiciones de entrada inválidas (fallback de reglas de negocio). */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "Solicitud inválida", List.of(safeMessage(ex, "argumento inválido")), req);
    }

    // ---------- 404 Not Found ----------

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<?> handleAccountNotFound(AccountNotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, "Recurso no encontrado", List.of(safeMessage(ex, "account not found")), req);
    }

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<?> handleCustomerNotFound(CustomerNotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, "Recurso no encontrado", List.of(safeMessage(ex, "customer not found")), req);
    }

    /** Por compatibilidad si en algún punto se lanza NoSuchElementException. */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<?> handleNoSuchElement(NoSuchElementException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, "Recurso no encontrado", List.of(safeMessage(ex, "no such element")), req);
    }

    // ---------- 409 Conflict (DB/negocio) ----------

    /** Conflictos de integridad (UNIQUE, FK...). */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleConflict(DataIntegrityViolationException ex, HttpServletRequest req) {
        log.warn("DB conflict: {}", ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage());
        return build(HttpStatus.CONFLICT, "Conflicto de datos",
                List.of("La operación viola una restricción de base de datos"), req);
    }

    /** Email duplicado u otros conflictos de negocio controlados. */
    @ExceptionHandler(EmailAlreadyInUseException.class)
    public ResponseEntity<?> handleEmailConflict(EmailAlreadyInUseException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, "Conflicto de datos", List.of(safeMessage(ex, "email ya registrado")), req);
    }

    // ---------- 500 Internal Server Error ----------

    /** Fallback final: cualquier excepción no mapeada explícitamente. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleUnexpected(Exception ex, HttpServletRequest req) {
        log.error("Unexpected error", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno",
                List.of("Ha ocurrido un error inesperado. Inténtalo más tarde."), req);
    }

    // ---------- Helpers ----------

    private String fmt(FieldError e) {
        return e.getField() + " " + e.getDefaultMessage();
    }

    /** Evita mensajes nulos/vacíos y aporta uno por defecto. */
    private String safeMessage(Throwable ex, String fallback) {
        return (ex.getMessage() == null || ex.getMessage().isBlank()) ? fallback : ex.getMessage();
    }

    /** Construye el cuerpo homogéneo de error con timestamp ISO-8601 legible. */
    private ResponseEntity<Map<String, Object>> build(HttpStatus status, String message,
                                                      List<String> details, HttpServletRequest req) {
        Map<String, Object> body = Map.of(
                "timestamp", java.time.OffsetDateTime.now().toString(),
                "path", req.getRequestURI(),
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", message,
                "details", details
        );
        return ResponseEntity.status(status).body(body);
    }
}
