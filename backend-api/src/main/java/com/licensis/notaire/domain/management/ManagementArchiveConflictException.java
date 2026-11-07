package com.licensis.notaire.domain.management;

import java.util.List;
import java.util.Objects;

/**
 * Archive rejected because procedure folders are still on hold.
 * Clients receive HTTP 409 with {@code message} and {@code waitingFolderNumbers}.
 */
public class ManagementArchiveConflictException extends ManagementArchiveException {

    private final List<Integer> waitingFolderNumbers;

    public ManagementArchiveConflictException(String message, List<Integer> waitingFolderNumbers) {
        super(Objects.requireNonNull(message, "message"));
        this.waitingFolderNumbers = List.copyOf(
                Objects.requireNonNull(waitingFolderNumbers, "waitingFolderNumbers"));
    }

    public List<Integer> waitingFolderNumbers() {
        return waitingFolderNumbers;
    }
}
