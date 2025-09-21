package com.sergiom.minicorebank.accounts;

import java.util.Random;

public final class IbanGenerator {
    private static final Random RND = new Random();

    private IbanGenerator(){}

    public static String newEsIban(){
        // IBAN fake para demo: ES + 22 dígitos
        StringBuilder sb = new StringBuilder("ES");
        for (int i = 0; i < 22; i ++) sb.append(RND.nextInt(10));
        return sb.toString();
    }
}
