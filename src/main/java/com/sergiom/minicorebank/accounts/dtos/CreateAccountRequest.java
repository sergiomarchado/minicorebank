package com.sergiom.minicorebank.accounts.dtos;

import com.sergiom.minicorebank.common.CurrencyCode;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateAccountRequest(
        @NotNull UUID customerId,
        @NotNull CurrencyCode currency
) {}
