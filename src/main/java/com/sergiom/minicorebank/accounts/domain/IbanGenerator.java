package com.sergiom.minicorebank.accounts.domain;

import java.security.SecureRandom;

/**
 * Generador y validador de IBAN para España (ES).

 * IBAN: Para España tiene 24 caracteres:
 *   "ES" + 2 dígitos de control + 20 dígitos (BBAN).

 * ¿Cómo se calculan los dígitos de control?
 * - Norma ISO-13616 (método mod-97):
 *   1) Construye la cadena: BBAN + COUNTRY + "00".
 *   2) Reemplaza letras por números (A=10, B=11, ..., Z=35).
 *   3) Calcula el resto mod 97 iterando carácter a carácter.
 *   4) Dígitos de control = 98 - (resto).

 * ¿Por qué SecureRandom?
 * - Para evitar patrones predecibles en los dígitos generados (mejor práctica).

 * Importante:
 * - La unicidad del IBAN se garantiza con un índice UNIQUE en BD.
 * - Si al persistir hay colisión, reintenta generando otro IBAN (esa lógica va en el servicio).
 */
public final class IbanGenerator {

    /** Fuente de aleatoriedad criptográfica. */
    private static final SecureRandom RND = new SecureRandom();

    private IbanGenerator() {
        // Utilidad estática (no instanciable).
    }

    /**
     * Genera un IBAN ES válido (24 caracteres):
     *  - "ES" + 2 dígitos de control + 20 dígitos BBAN aleatorios.
     *
     * @return IBAN español válido (por ejemplo: "ES12XXXXXXXXXX...").
     */
    public static String newEsIban() {
        // 1) Generamos un BBAN (20 dígitos para ES)
        String bban = randomDigits(20);

        // 2) Calculamos los dígitos de control para país ES
        String checkDigits = computeCheckDigits("ES", bban);

        // 3) Montamos el IBAN final
        return "ES" + checkDigits + bban;
    }

    /**
     * Verifica si un IBAN cumple la regla general de ISO-13616 (longitud y dígitos de control).
     * Nota: esta validación es sintáctica, no comprueba estructura específica del BBAN por banco.
     *
     * @param iban IBAN a validar (con letras mayúsculas y sin espacios).
     * @return true si pasa el algoritmo de validación, false en caso contrario.
     */
    public static boolean isValidIban(String iban) {
        if (iban == null || iban.length() < 4) return false;

        // Normalizamos: sin espacios y en mayúsculas
        String normalized = iban.replace(" ", "").toUpperCase();

        // Longitud mínima IBAN es 15; ES tiene 24 (opcionalmente puedes reforzar longitud exacta para ES)
        if (normalized.length() < 15) return false;

        // Reordenamos: del país+check al final
        String rearranged = normalized.substring(4) + normalized.substring(0, 4);

        // Calculamos mod 97; debe ser 1 para IBAN válido
        return mod97(replaceLetters(rearranged)) == 1;
    }

    // ----------------------------------------------------------------------
    // Helpers privados
    // ----------------------------------------------------------------------

    /** Genera una cadena de N dígitos decimales (que es 20, pero así lo hacemos reutilizable por si aca ;). */
    private static String randomDigits(int n) {
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            sb.append(RND.nextInt(10)); // 0..9
        }
        return sb.toString();
    }

    /**
     * Calcula los dos dígitos de control para un país y BBAN dados (ISO-13616).
     * Pasos:
     *  - temp = BBAN + COUNTRY + "00"
     *  - Sustituye letras por números (A=10..Z=35)
     *  - check = 98 - (temp mod 97)
     *  - Devuelve check en 2 dígitos (con cero a la izquierda si hace falta)
     */
    private static String computeCheckDigits(String countryCode, String bban) {
        String temp = bban + countryCode + "00";
        int remainder = mod97(replaceLetters(temp));
        int check = 98 - remainder;
        return (check < 10) ? "0" + check : Integer.toString(check);
    }

    /**
     * Reemplaza letras por sus valores numéricos (A=10..Z=35) dejando los dígitos igual.
     * No usa BigInteger: procesa carácter a carácter para permitir longitud arbitraria.
     */
    private static String replaceLetters(String input) {
        StringBuilder out = new StringBuilder(input.length() * 2); // letras se expanden a 2 dígitos
        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);
            if (ch >= 'A' && ch <= 'Z') {
                int val = ch - 'A' + 10; // A=10, B=11, ..., Z=35
                out.append(val);
            } else if (ch >= '0' && ch <= '9') {
                out.append(ch);
            } else {
                // Caracteres no válidos en IBAN (espacios, guiones, etc.) → ignorar/romper.
                // Aquí lanzamos IllegalArgumentException para dejarlo claro.
                throw new IllegalArgumentException("Invalid character in IBAN: " + ch);
            }
        }
        return out.toString();
    }

    /**
     * Calcula (número representado por la cadena) mod 97 sin desbordar,
     * iterando carácter a carácter.
     */
    private static int mod97(String numericString) {
        int remainder = 0;
        for (int i = 0; i < numericString.length(); i++) {
            int digit = numericString.charAt(i) - '0'; // 0..9
            remainder = (remainder * 10 + digit) % 97;
        }
        return remainder;
    }
}
