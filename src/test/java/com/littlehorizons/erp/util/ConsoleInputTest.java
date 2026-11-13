package com.littlehorizons.erp.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Scanner;
import java.util.function.Consumer;
import static org.junit.jupiter.api.Assertions.*;

class ConsoleInputTest {
    private ConsoleInput input(String text) {
        return new ConsoleInput(new Scanner(text), new PrintStream(new ByteArrayOutputStream()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "0.0", "5", "5.0", "4.5", "4,5", "4.50", " 3.2 "})
    void acceptsExactGrades(String text) {
        BigDecimal expected = new BigDecimal(text.trim().replace(',', '.')).setScale(1);
        assertEquals(expected, input(text + "\n").readAcademicGrade(""));
    }

    @ParameterizedTest
    @ValueSource(strings = {"-0.04", "5.04", "-0.1", "5.1", "4.55", "4.54", "", "abc", "NaN", "Infinity", "4,5.1"})
    void rejectsGradeThenReadsNextValue(String text) {
        assertEquals(new BigDecimal("3.0"), input(text + "\n3\n").readAcademicGrade(""));
    }

    @ParameterizedTest
    @ValueSource(strings = {"2026-02-29", "2024-02-30", "2026-04-31", "2026-13-01", "2026-00-01",
            "2026-01-00", "2026-2-01", "26-02-01", "0000-01-01", "", "abc"})
    void rejectsImpossibleOrMalformedDates(String text) {
        assertEquals(LocalDate.of(2024, 2, 29), input(text + "\n2024-02-29\n").readDate(""));
    }

    @ParameterizedTest
    @ValueSource(strings = {"2024-02-29", "2000-02-29", "2026-12-31", "2026-01-01"})
    void acceptsStrictDates(String text) {
        assertEquals(LocalDate.parse(text), input(text + "\n").readDate(""));
    }

    @Test void retriesRequiredStringsAndKeepsOptionalEmptyDistinctFromEof() {
        assertEquals("name", input("\n   \n name \n").readNonEmptyString(""));
        assertNull(input("   \n").readOptionalString(""));
        assertThrows(EndOfInputException.class, () -> input("").readOptionalString(""));
    }

    @Test void retriesInvalidIntegersAndOverflow() {
        assertEquals(2, input("\nabc\n2147483648\n0\n4\n2\n").readInt("", 1, 3));
    }

    @Test void retriesInvalidBooleansWithoutInventingAnAnswer() {
        assertTrue(input("\nmaybe\ns\n").readBoolean(""));
        assertFalse(input("n\n").readBoolean(""));
    }

    @Test void preservesExistingNonnegativeDecimalBehavior() {
        assertEquals(new BigDecimal("3.46"), input("\nabc\n-1\n3,456\n").readPositiveBigDecimal(""));
    }

    @Test void everyReaderSignalsEofAndClosedScannerWithoutLooping() {
        java.util.List<Consumer<ConsoleInput>> readers = java.util.List.of(
                r -> r.readNonEmptyString(""), r -> r.readOptionalString(""),
                r -> r.readInt("", 1, 3), r -> r.readPositiveInt(""),
                r -> r.readPositiveBigDecimal(""), r -> r.readAcademicGrade(""),
                r -> r.readDate(""), r -> r.readBoolean(""), ConsoleInput::pause);
        for (Consumer<ConsoleInput> reader : readers) {
            assertThrows(EndOfInputException.class, () -> reader.accept(input("")));
            Scanner scanner = new Scanner("");
            scanner.close();
            ConsoleInput closed = new ConsoleInput(scanner, new PrintStream(new ByteArrayOutputStream()));
            assertThrows(EndOfInputException.class, () -> reader.accept(closed));
        }
    }

    @Test void invalidInputFollowedByEofCancelsOperation() {
        assertThrows(EndOfInputException.class, () -> input("bad\n").readAcademicGrade(""));
        assertThrows(EndOfInputException.class, () -> input("bad\n").readDate(""));
    }
}
