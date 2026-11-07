package com.licensis.notaire.adapter.in.web.person;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.licensis.notaire.adapter.in.web.support.ErrorResponses;
import com.licensis.notaire.domain.person.PersonDuplicateException;
import com.licensis.notaire.domain.person.PersonInUseException;
import com.licensis.notaire.domain.person.PersonNotFoundException;
import com.licensis.notaire.domain.person.PersonOperationFailedException;

/**
 * Maps person domain exceptions to the HTTP shapes clients already receive from
 * {@link PersonController} write methods (#577).
 */
@RestControllerAdvice(assignableTypes = PersonController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PersonExceptionHandler {

    /** Same JSON keys as the former controller {@code duplicateBody} Map. */
    public record PersonDuplicateConflict(String message, Integer existingPersonId) {
    }

    @ExceptionHandler(PersonNotFoundException.class)
    ResponseEntity<Void> notFound() {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(PersonDuplicateException.class)
    ResponseEntity<PersonDuplicateConflict> duplicate(PersonDuplicateException duplicate) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new PersonDuplicateConflict(duplicate.getMessage(), duplicate.existingPersonId()));
    }

    @ExceptionHandler(PersonInUseException.class)
    ResponseEntity<String> inUse(PersonInUseException locked) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(locked.getMessage());
    }

    @ExceptionHandler(PersonOperationFailedException.class)
    ResponseEntity<Object> failed(PersonOperationFailedException failed) {
        return ErrorResponses.createFailed(failed.causeAsException());
    }
}
