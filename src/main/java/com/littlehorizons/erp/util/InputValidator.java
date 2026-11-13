package com.littlehorizons.erp.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Scanner;

/** Console validation facade. Readers signal EOF with EndOfInputException. */
public final class InputValidator {
    private InputValidator() { }

    private static final class Holder {
        private static final ConsoleInput INPUT = new ConsoleInput(new Scanner(System.in), System.out);
    }

    public static String readNonEmptyString(String prompt) { return Holder.INPUT.readNonEmptyString(prompt); }
    public static String readOptionalString(String prompt) { return Holder.INPUT.readOptionalString(prompt); }
    public static int readInt(String prompt, int min, int max) { return Holder.INPUT.readInt(prompt, min, max); }
    public static int readPositiveInt(String prompt) { return Holder.INPUT.readPositiveInt(prompt); }
    public static BigDecimal readPositiveBigDecimal(String prompt) { return Holder.INPUT.readPositiveBigDecimal(prompt); }
    public static BigDecimal readAcademicGrade(String prompt) { return Holder.INPUT.readAcademicGrade(prompt); }
    public static LocalDate readDate(String prompt) { return Holder.INPUT.readDate(prompt); }
    public static boolean readBoolean(String prompt) { return Holder.INPUT.readBoolean(prompt); }
    public static void pause() { Holder.INPUT.pause(); }
}