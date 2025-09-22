# MiniCoreBank API

> API REST de núcleo bancario ligero (mini core) desarrollada en **Java & Spring Boot**. 
> Gestiona **clientes** y **cuentas**, registra **movimientos** en un **libro mayor (ledger)** y calcula el saldo a partir de los asientos, con **validaciones**, **manejo uniforme de errores** y **documentación OpenAPI**.

---

## ⚙️ Tecnologías

- **Java 21**, **Spring Boot 3**
- Spring Web, Spring Data JPA (Hibernate), Spring Validation
- Spring Security (Basic Auth en desarrollo; **JWT** previsto)
- **PostgreSQL 16**, **Flyway** (migraciones)
- **OpenAPI / Swagger-UI** (springdoc)
- Maven, Testcontainers (base para tests), JaCoCo
- Docker (compose para DB)

---

## 🎬 Demo (vídeo)
> _PENDIENTE POR GRABAR 
`[➡️ Enlace al vídeo]()`

## 📸 Capturas
> _PENDIENTE POR INCLUIR

---

## 🧭 Endpoints principales (v1)

| Recurso | Método & Ruta | Descripción |
|---|---|---|
| **Customers** | `POST /api/v1/customers` | Alta de cliente (name, email único). |
|  | `GET /api/v1/customers/{id}` | Detalle de cliente. |
| **Accounts** | `POST /api/v1/accounts` | Apertura de cuenta (cliente existente, moneda, IBAN ES simulado). |
|  | `POST /api/v1/accounts/{id}/deposit` | Depósito (amountMinor, description). |
|  | `GET /api/v1/accounts/{id}/balance` | Saldo calculado a partir del ledger. |

📚 **OpenAPI/Swagger**: `http://localhost:8080/swagger-ui.html` → (sirve `/v3/api-docs`).

---

## 🧱 Diseño (breve)
- **Arquitectura por capas / DDD-lite**: `api` (controladores/DTOs) · `application` (servicios) · `domain` (entidades y reglas) · `infrastructure` (repositorios JPA).
- **Ledger**: el saldo no se persiste en `Account`; se **deriva** sumando asientos (`LedgerEntry`) → trazabilidad y auditoría.
- **Errores homogéneos**: `GlobalExceptionHandler` devuelve `{timestamp, path, status, message, details}`.
- **Seguridad**: filtro para `/api/**` (**Basic Auth** en desarrollo). **JWT** previsto con `spring-boot-starter-oauth2-resource-server`.

---

## 🚀 Arranque local

### 1) Base de datos
```bash
# PostgreSQL vía Docker (puedes adaptar puertos/clave en docker-compose.yml)
docker compose up -d
# o alternativamente: usar tu PostgreSQL local y ajustar application.yml
```
Flyway migrará el esquema al iniciar la app.

### 2) Aplicación
```bash
# Desde el proyecto
./mvnw spring-boot:run
# o construir jar
./mvnw clean package && java -jar target/minicorebank-0.0.1-SNAPSHOT.jar
```

### 3) Credenciales (temporal, desarrollo)
- **Basic Auth**: usuario generado por Spring (`user`) y contraseña aleatoria en consola.  
  _Más adelante se activará **JWT** (Bearer) y se retirará Basic._

---

## 🔎 Ejemplos rápidos (cURL)

```bash
# 1) Alta de cliente
curl -u user:PASS -H "Content-Type: application/json" -d '{"name":"Ada","email":"ada@example.com"}'   http://localhost:8080/api/v1/customers

# 2) Apertura de cuenta (sustituye CUSTOMER_ID)
curl -u user:PASS -H "Content-Type: application/json" -d '{"customerId":"<CUSTOMER_ID>","currency":"EUR"}'   http://localhost:8080/api/v1/accounts

# 3) Depósito (sustituye ACCOUNT_ID)
curl -u user:PASS -H "Content-Type: application/json" -d '{"amountMinor":1000,"description":"Ingreso inicial"}'   http://localhost:8080/api/v1/accounts/<ACCOUNT_ID>/deposit

# 4) Consulta de saldo
curl -u user:PASS http://localhost:8080/api/v1/accounts/<ACCOUNT_ID>/balance
```

---

## 🗂️ Estructura (módulo `accounts` como ejemplo)

```
accounts/
  api/               # Controladores + DTOs
  application/       # Servicios de aplicación
  domain/            # Entidades + reglas de dominio
  infrastructure/    # Repositorios JPA
```

---

## 🗺️ Roadmap / Futuras mejoras
- 🔐 **JWT** (Resource Server) + roles/autoridades.
- 🔁 **Idempotencia** para operaciones de escritura (p. ej. depósitos) con claves únicas por `txnId`.
- 🔄 **Transferencias** (doble asiento: débito/crédito) y conciliación.
- 🧪 **Tests** de integración con Testcontainers.
- 📊 **Métricas**/tracing (Actuator + Micrometer/Prometheus).
- 📝 **Más documentación** (diagrama de secuencia, ADRs breves).
- 🧰 CLI de utilidades para datos de demo.

---

## ⚖️ Licencia
MIT

---

## 👤 Autor
Sergio M. — Backend (Java)
