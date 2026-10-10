package com.licensis.notaire.adapter.in.web.support;

import com.licensis.notaire.exception.BusinessValidationException;

import java.util.ArrayList;
import java.util.List;

/**
 * Checks for request fields that controllers bind to shared {@code Dto*} classes, where a bean
 * validation constraint would also mark the field required in every response schema that reuses
 * the DTO (issue #655, lesson from slice 1). A failed check throws
 * {@link BusinessValidationException}, which {@code GlobalExceptionHandler} answers with
 * {@code 400} and the standard {@code ErrorResponse}; the message uses the same
 * {@code "field: reason"} shape as bean validation.
 */
public final class RequiredFields {

    private RequiredFields() {
    }

    /**
     * Starts a check that collects every missing field and throws one 400 naming them all,
     * {@code "a: es obligatorio; b: es obligatorio"} (issue #655, incomplete PUT bodies).
     */
    public static Check check() {
        return new Check();
    }

    /** Collects missing fields; see {@link #check()}. */
    public static final class Check {

        private final List<String> missing = new ArrayList<>();

        private Check() {
        }

        /** Records {@code field} as missing unless {@code present}. */
        public Check present(boolean present, String field) {
            if (!present) {
                missing.add(field);
            }
            return this;
        }

        /** Records {@code field} as missing when {@code value} is {@code null}. */
        public Check notNull(Object value, String field) {
            return present(value != null, field);
        }

        /** Throws a 400 naming every missing field, if any. */
        public void orThrow() {
            if (!missing.isEmpty()) {
                StringBuilder message = new StringBuilder();
                for (String field : missing) {
                    message.append(message.isEmpty() ? "" : "; ").append(field).append(": es obligatorio");
                }
                throw new BusinessValidationException(message.toString());
            }
        }
    }

    /** Returns {@code value}, or throws a 400 naming {@code field} when it is {@code null}. */
    public static <T> T require(T value, String field) {
        if (value == null) {
            throw new BusinessValidationException(field + ": es obligatorio");
        }
        return value;
    }

    /**
     * Parses a required enum value, or throws a 400 naming {@code field} and the accepted values
     * when it is missing or unknown.
     */
    public static <E extends Enum<E>> E requireEnum(String value, Class<E> type, String field) {
        return parseEnum(require(value, field), type, field);
    }

    /** Parses an enum value, or throws a 400 naming {@code field} and the accepted values. */
    public static <E extends Enum<E>> E parseEnum(String value, Class<E> type, String field) {
        try {
            return Enum.valueOf(type, value);
        } catch (IllegalArgumentException e) {
            StringBuilder accepted = new StringBuilder();
            for (E constant : type.getEnumConstants()) {
                accepted.append(accepted.isEmpty() ? "" : ", ").append(constant.name());
            }
            throw new BusinessValidationException(field + ": valor no válido '" + value + "'; valores aceptados: "
                    + accepted);
        }
    }
}
