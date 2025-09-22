package com.sergiom.minicorebank.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuración de seguridad HTTP (modo DESARROLLO: todo abierto).
 *
 * Ideas clave:
 * - Spring Security aplica una cadena de filtros (SecurityFilterChain) a cada petición.
 * - Aquí definimos QUÉ rutas requieren autenticación y QUÉ mecanismo usamos.
 * - Para poder probar fácilmente desde Swagger/UI, dejamos TODO en permitAll().
 *
 * ¿Por qué no hay login aquí?
 * - Si activamos Basic o FormLogin, el navegador muestra el pop-up de usuario/contraseña.
 * - En desarrollo lo evitamos; cuando integremos JWT, activaremos el Resource Server.
 *
 * Cuando metas JWT:
 * 1) Cambia la regla de /api/** a authenticated().
 * 2) Activa el resource server con .oauth2ResourceServer(oauth -> oauth.jwt()).
 * (ver comentario en el método security()).
 */
@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain security(HttpSecurity http) throws Exception {
        return http
                // API stateless: sin sesión en el servidor.
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // CSRF se pensó para formularios; en APIs REST lo desactivamos.
                .csrf(csrf -> csrf.disable())

                // CORS habilitado para poder llamar desde frontends locales (React/Angular).
                .cors(Customizer.withDefaults())

                // Autorización: TODO PERMITIDO en desarrollo (Swagger, Actuator y /api/**).
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/actuator/**"
                        ).permitAll()
                        .requestMatchers("/api/**").permitAll() // <— API abierta para probar desde Swagger
                        .anyRequest().permitAll()
                )

                // OJO: NO activamos httpBasic() ni formLogin(), así evitamos el pop-up del navegador.
                //
                // Cuando tengas JWT:
                //   .authorizeHttpRequests(auth -> auth
                //       .requestMatchers("/v3/api-docs/**","/swagger-ui/**","/swagger-ui.html","/actuator/**").permitAll()
                //       .requestMatchers("/api/**").authenticated()
                //       .anyRequest().denyAll()
                //   )
                //   .oauth2ResourceServer(oauth -> oauth.jwt(Customizer.withDefaults()));

                .build();
    }

    /**
     * CORS sencillo para desarrollo:
     * - Permite orígenes típicos de front local.
     * - Métodos y cabeceras amplios para no bloquear pruebas.
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:4200"));
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}
