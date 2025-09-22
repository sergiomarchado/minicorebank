package com.sergiom.minicorebank.common;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración explícita de OpenAPI. Evita NPEs si springdoc
 * no consigue inferir el bean por sí solo.
 */
@Configuration
public class SpringdocConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MiniCoreBank API")
                        .version("v1")
                        .description("Mini core bancario: cuentas, clientes y ledger.")
                );
    }
}
