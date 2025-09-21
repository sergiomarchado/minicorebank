package com.sergiom.minicorebank.customers.dtos;

import java.util.UUID;

public record CustomerResponse(UUID id, String fullName, String email) {}
