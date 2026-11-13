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
final class ConsoleInput {

    private final Scanner scanner;
    private final java.io.PrintStream output;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(java.time.format.ResolverStyle.STRICT);

    ConsoleInput(Scanner scanner, java.io.PrintStream output) {
        this.scanner = java.util.Objects.requireNonNull(scanner);
        this.output = java.util.Objects.requireNonNull(output);
    }

    /**
     * Reads a non-empty string.
     *
     * @param prompt Message prompt to display.
     * @return Valid trimmed string.
     */
    public String readNonEmptyString(String prompt) {
        while (true) {
            output.print(prompt);
            String line = readLine();
            if (!line.isEmpty()) {
                return line;
            }
            output.println("❌ Entrada inválida. El campo no puede estar vacío. Intente de nuevo.");
        }
    }

    /**
     * Reads an optional string (can be empty).
     *
     * @param prompt Message prompt to display.
     * @return Trimmed string or null if empty.
     */
    public String readOptionalString(String prompt) {
        output.print(prompt);
        String line = readLine();
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
    public int readInt(String prompt, int min, int max) {
        if (min > max) throw new IllegalArgumentException("Invalid integer range.");
        while (true) {
            output.print(prompt);
            String input = readLine();
            try {
                int val = Integer.parseInt(input);
                if (val >= min && val <= max) {
                    return val;
                }
                output.printf("❌ Ingrese un número entre %d y %d.%n", min, max);
            } catch (NumberFormatException e) {
                output.println("❌ Formato numérico inválido. Intente de nuevo.");
            }
        }
    }

    /**
     * Reads an integer greater than zero.
     *
     * @param prompt Message prompt to display.
     * @return Positive integer.
     */
    public int readPositiveInt(String prompt) {
        return readInt(prompt, 1, Integer.MAX_VALUE);
    }

    /**
     * Reads a BigDecimal value greater than or equal to zero.
     *
     * @param prompt Message prompt to display.
     * @return Valid positive BigDecimal.
     */
    public BigDecimal readPositiveBigDecimal(String prompt) {
        while (true) {
            output.print(prompt);
            String input = readLine().replace(",", ".");
            try {
                BigDecimal val = new BigDecimal(input);
                if (val.compareTo(BigDecimal.ZERO) >= 0) {
                    return val.setScale(2, RoundingMode.HALF_UP);
                }
                output.println("❌ El valor debe ser mayor o igual a cero.");
            } catch (NumberFormatException e) {
                output.println("❌ Formato numérico decimal inválido. Intente de nuevo.");
            }
        }
    }

    /**
     * Reads a grade in the inclusive range 0.0..5.0 without rounding.
     *
     * @param prompt Message prompt to display.
     * @return BigDecimal grade between 0.0 and 5.0.
     */
    public BigDecimal readAcademicGrade(String prompt) {
        while (true) {
            output.print(prompt);
            String input = readLine().replace(",", ".");
            try {
                return GradeValidator.validate(new BigDecimal(input));
            } catch (IllegalArgumentException e) {
                output.println("Nota inválida: use un valor entre 0.0 y 5.0 con un decimal, sin redondeo.");
            }
        }
    }

    /**
     * Reads a date in yyyy-MM-dd format.
     *
     * @param prompt Message prompt to display.
     * @return Valid LocalDate.
     */
    public LocalDate readDate(String prompt) {
        while (true) {
            output.print(prompt);
            String input = readLine();
            try {
                if (!input.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}") || input.startsWith("0000-")) {
                    throw new DateTimeParseException("Invalid date format", input, 0);
                }
                return LocalDate.parse(input, DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                output.println("❌ Fecha inválida. Utilice el formato AAAA-MM-DD (ej: 2026-03-15).");
            }
        }
    }

    /**
     * Reads a yes/no boolean confirmation.
     *
     * @param prompt Message prompt to display.
     * @return true for S/Si/Yes, false otherwise.
     */
    public boolean readBoolean(String prompt) {
        while (true) {
            output.print(prompt + " (s/n): ");
            String input = readLine().toLowerCase(java.util.Locale.ROOT);
            if (input.equals("s") || input.equals("si") || input.equals("y") || input.equals("yes")) {
                return true;
            }
            if (input.equals("n") || input.equals("no")) {
                return false;
            }
            output.println("❌ Ingrese 's' para Sí o 'n' para No.");
        }
    }

    /**
     * Pauses the console until the user presses Enter.
     */
    public void pause() {
        output.print("\nPresione ENTER para continuar...");
        readLine();
    }

    private String readLine() {
        try {
            if (scanner.hasNextLine()) return scanner.nextLine().trim();
        } catch (IllegalStateException e) {
            throw new EndOfInputException();
        }
        throw new EndOfInputException();
    }
}
