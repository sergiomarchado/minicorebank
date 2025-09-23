package com.sergiom.minicorebank;

import com.sergiom.minicorebank.accounts.api.dtos.AccountEntryResponse;
import com.sergiom.minicorebank.accounts.api.dtos.AccountResponse;
import com.sergiom.minicorebank.accounts.api.dtos.BalanceResponse;
import com.sergiom.minicorebank.accounts.api.dtos.CreateAccountRequest;
import com.sergiom.minicorebank.accounts.api.dtos.DepositRequest;
import com.sergiom.minicorebank.common.CurrencyCode;
import com.sergiom.minicorebank.customers.api.dto.CreateCustomerRequest;
import com.sergiom.minicorebank.customers.api.dto.CustomerResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ApiE2ETests {

    @Autowired
    TestRestTemplate restTemplate;

    @Test
    void full_happy_path_customer_account_deposit_balance_and_entries() {
        var http = restTemplate.withBasicAuth("test", "test123");

        // --- Alta de cliente ---
        var email = "harry+" + UUID.randomUUID() + "@example.com";
        var createCustomer = new CreateCustomerRequest("Harry Potter", email);

        var customer = http.postForEntity("/api/v1/customers", createCustomer, CustomerResponse.class)
                .getBody();
        assertThat(customer).isNotNull();
        var customerId = customer.id();
        assertThat(customer.name()).isEqualTo("Harry Potter");
        assertThat(customer.email()).isEqualTo(email);

        // --- Crear cuenta ---
        var createAccount = new CreateAccountRequest(customerId, CurrencyCode.EUR);
        var account = http.postForEntity("/api/v1/accounts", createAccount, AccountResponse.class)
                .getBody();
        assertThat(account).isNotNull();
        var accountId = account.id();
        assertThat(account.currency()).isEqualTo(CurrencyCode.EUR);
        assertThat(account.iban()).startsWith("ES");

        // --- Depósito ---
        var deposit = new DepositRequest(1_000L, "Ingreso inicial");
        var afterDeposit = http.postForEntity(
                "/api/v1/accounts/{id}/deposit",
                deposit,
                BalanceResponse.class,
                accountId
        ).getBody();
        assertThat(afterDeposit).isNotNull();
        assertThat(afterDeposit.balanceMinor()).isEqualTo(1_000L);

        // --- Consultar saldo ---
        var balance = http.getForEntity(
                "/api/v1/accounts/{id}/balance",
                BalanceResponse.class,
                accountId
        ).getBody();
        assertThat(balance).isNotNull();
        assertThat(balance.balanceMinor()).isEqualTo(1_000L);

        // --- Consultar movimientos recientes ---
        ResponseEntity<List<AccountEntryResponse>> entriesResp = http.exchange(
                "/api/v1/accounts/{id}/entries?size=10",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                },
                accountId
        );
        var entries = entriesResp.getBody();
        assertThat(entries).isNotNull();
        assertThat(entries.size()).isGreaterThanOrEqualTo(1);
        assertThat(entries.getFirst().amountMinor()).isEqualTo(1_000L);
        assertThat(entries.getFirst().description()).isEqualTo("Ingreso inicial");
        assertThat(entries.getFirst().direction().name()).isEqualTo("CREDIT");
    }
}
