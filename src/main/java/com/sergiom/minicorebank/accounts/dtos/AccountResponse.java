package com.sergiom.minicorebank.accounts.dtos;

import com.sergiom.minicorebank.common.CurrencyCode;

import java.util.UUID;

public record AccountResponse(UUID id, String iban, CurrencyCode currency, String status) {}
