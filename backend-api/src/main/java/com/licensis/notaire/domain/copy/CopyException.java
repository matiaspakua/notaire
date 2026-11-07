package com.licensis.notaire.domain.copy;

/** Base type for copy domain failures mapped by {@code CopyExceptionHandler}. */
public class CopyException extends RuntimeException {

    protected CopyException(String message) {
        super(message);
    }
}
