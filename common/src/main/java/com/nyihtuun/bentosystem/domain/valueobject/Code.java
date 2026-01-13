package com.nyihtuun.bentosystem.domain.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.security.SecureRandom;
import java.util.UUID;

@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
public final class Code {
    private String value;

    private Code(String value) {
        this.value = value;
    }

    public static Code generate() {
        return new Code(CodeGenerator.generate());
    }

    public String value() {
        return value;
    }

    private static final class CodeGenerator {

        private static final SecureRandom RANDOM = new SecureRandom();

        private static final char[] LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
        private static final char[] NUM =
                "0123456789".toCharArray();

        private CodeGenerator() {
            // prevent instantiation
        }

        public static String generate() {
            char[] code = new char[7];

            // 2 leading alphabets
            for (int i = 0; i < 2; i++) {
                code[i] = LETTERS[RANDOM.nextInt(LETTERS.length)];
            }

            // remaining 5 alphanumeric
            for (int i = 2; i < 7; i++) {
                code[i] = NUM[RANDOM.nextInt(NUM.length)];
            }

            return new String(code);
        }
    }
}
