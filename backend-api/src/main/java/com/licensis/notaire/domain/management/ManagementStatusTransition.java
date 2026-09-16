package com.licensis.notaire.domain.management;

/**
 * Domain value object representing a valid status transition for management.
 * Encapsulates the validation rule: can a management transition from one status to another
 * given its workflow definition?
 */
public class ManagementStatusTransition {

    private final String originStatusName;
    private final String destinationStatusName;

    public ManagementStatusTransition(String originStatusName, String destinationStatusName) {
        this.originStatusName = originStatusName;
        this.destinationStatusName = destinationStatusName;
    }

    public String getOriginStatusName() {
        return originStatusName;
    }

    public String getDestinationStatusName() {
        return destinationStatusName;
    }

    public boolean matches(String currentStatusName, String targetStatusName) {
        return originStatusName.equals(currentStatusName)
                && destinationStatusName.equals(targetStatusName);
    }
}
