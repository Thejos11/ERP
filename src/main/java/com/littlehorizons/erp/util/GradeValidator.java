package com.littlehorizons.erp.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Shared domain rule matching valor_nota DECIMAL(2,1) and its 0..5 CHECK. */
public final class GradeValidator {
    private static final BigDecimal MAX_GRADE = new BigDecimal("5.0");

    private GradeValidator() { }

    public static BigDecimal validate(BigDecimal original) {
        if (original == null || original.compareTo(BigDecimal.ZERO) < 0
                || original.compareTo(MAX_GRADE) > 0) {
            throw new IllegalArgumentException("La nota debe estar entre 0.0 y 5.0.");
        }
        try {
            return original.setScale(1, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("La nota admite un decimal, sin redondeo.");
        }
    }
}
