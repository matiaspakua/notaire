package com.licensis.notaire.application.usecase.testimony;

/**
 * Data CU44 asks for when a withdrawn testimony is re-entered.
 *
 * @param cardNumber cartón number, 0 when not given
 * @param observedByRegistry whether the Registro returned the testimony observed
 * @param notes observations, mandatory when the testimony was observed
 */
public record TestimonyReentry(int cardNumber, boolean observedByRegistry, String notes) {

    public static TestimonyReentry empty() {
        return new TestimonyReentry(0, false, null);
    }

    public boolean isMissingRequiredNotes() {
        return observedByRegistry && (notes == null || notes.isBlank());
    }
}
