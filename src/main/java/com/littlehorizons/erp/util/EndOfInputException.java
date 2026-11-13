package com.littlehorizons.erp.util;

/** Signals cancellation of the console session without inventing a user response. */
public final class EndOfInputException extends RuntimeException {
    public EndOfInputException() {
        super("La entrada de consola ha finalizado.");
    }
}
