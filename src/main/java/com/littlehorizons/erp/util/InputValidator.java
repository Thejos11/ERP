package com.littlehorizons.erp.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/**
 * Utility class for reading and validating user inputs from the console.
 * Guarantees robust error handling and prevents crashes due to malformed input.
 */
public class InputValidator {

    private static final Scanner SCANNER = new Scanner(System.in);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private InputValidator() {
        // Prevent instantiation
    }

    /**
     * Reads a non-empty string.
     *
     * @param prompt Message prompt to display.
     * @return Valid trimmed string.
     */
    public static String readNonEmptyString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = SCANNER.nextLine().trim();
            if (!line.isEmpty()) {
                return line;
            }
            System.out.println("❌ Entrada inválida. El campo no puede estar vacío. Intente de nuevo.");
        }
    }

    /**
     * Reads an optional string (can be empty).
     *
     * @param prompt Message prompt to display.
     * @return Trimmed string or null if empty.
     */
    public static String readOptionalString(String prompt) {
        System.out.print(prompt);
        String line = SCANNER.nextLine().trim();
        return line.isEmpty() ? null : line;
    }

    /**
     * Reads an integer within a specified range [min, max].
     *
     * @param prompt Message prompt to display.
     * @param min Minimum allowable value.
     * @param max Maximum allowable value.
     * @return Valid integer.
     */
    public static int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String input = SCANNER.nextLine().trim();
            try {
                int val = Integer.parseInt(input);
                if (val >= min && val <= max) {
                    return val;
                }
                System.out.printf("❌ Ingrese un número entre %d y %d.%n", min, max);
            } catch (NumberFormatException e) {
                System.out.println("❌ Formato numérico inválido. Intente de nuevo.");
            }
        }
    }

    /**
     * Reads an integer greater than zero.
     *
     * @param prompt Message prompt to display.
     * @return Positive integer.
     */
    public static int readPositiveInt(String prompt) {
        return readInt(prompt, 1, Integer.MAX_VALUE);
    }

    /**
     * Reads a BigDecimal value greater than or equal to zero.
     *
     * @param prompt Message prompt to display.
     * @return Valid positive BigDecimal.
     */
    public static BigDecimal readPositiveBigDecimal(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = SCANNER.nextLine().trim().replace(",", ".");
            try {
                BigDecimal val = new BigDecimal(input);
                if (val.compareTo(BigDecimal.ZERO) >= 0) {
                    return val.setScale(2, RoundingMode.HALF_UP);
                }
                System.out.println("❌ El valor debe ser mayor o igual a cero.");
            } catch (NumberFormatException e) {
                System.out.println("❌ Formato numérico decimal inválido. Intente de nuevo.");
            }
        }
    }

    /**
     * Reads an academic grade strictly between 0.0 and 5.0 with up to 1 decimal place.
     *
     * @param prompt Message prompt to display.
     * @return BigDecimal grade between 0.0 and 5.0.
     */
    public static BigDecimal readAcademicGrade(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = SCANNER.nextLine().trim().replace(",", ".");
            try {
                BigDecimal grade = new BigDecimal(input).setScale(1, RoundingMode.HALF_UP);
                if (grade.compareTo(BigDecimal.ZERO) >= 0 && grade.compareTo(new BigDecimal("5.0")) <= 0) {
                    return grade;
                }
                System.out.println("❌ La calificación debe estar entre 0.0 y 5.0 (estándar colombiano).");
            } catch (NumberFormatException e) {
                System.out.println("❌ Formato de nota inválido (ejemplo: 4.5). Intente de nuevo.");
            }
        }
    }

    /**
     * Reads a date in yyyy-MM-dd format.
     *
     * @param prompt Message prompt to display.
     * @return Valid LocalDate.
     */
    public static LocalDate readDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = SCANNER.nextLine().trim();
            try {
                return LocalDate.parse(input, DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                System.out.println("❌ Fecha inválida. Utilice el formato AAAA-MM-DD (ej: 2026-03-15).");
            }
        }
    }

    /**
     * Reads a yes/no boolean confirmation.
     *
     * @param prompt Message prompt to display.
     * @return true for S/Si/Yes, false otherwise.
     */
    public static boolean readBoolean(String prompt) {
        while (true) {
            System.out.print(prompt + " (s/n): ");
            String input = SCANNER.nextLine().trim().toLowerCase();
            if (input.equals("s") || input.equals("si") || input.equals("y") || input.equals("yes")) {
                return true;
            }
            if (input.equals("n") || input.equals("no")) {
                return false;
            }
            System.out.println("❌ Ingrese 's' para Sí o 'n' para No.");
        }
    }

    /**
     * Pauses the console until the user presses Enter.
     */
    public static void pause() {
        System.out.print("\nPresione ENTER para continuar...");
        SCANNER.nextLine();
    }
}
