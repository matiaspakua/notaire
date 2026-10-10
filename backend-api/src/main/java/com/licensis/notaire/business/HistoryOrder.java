package com.licensis.notaire.business;

import java.util.Comparator;

/**
 * The one chronological order for a management's history (CU13, #1383).
 *
 * <p>{@code history.event_date} is a SQL {@code DATE}, so entries recorded on the same day
 * share the same instant. {@code idHistory} (insertion order) breaks the tie, which makes the
 * order total and stable across calls and databases. Every history read path uses this order.
 */
public final class HistoryOrder {

    /** Event date ascending, then {@code idHistory} ascending; nulls last. */
    public static final Comparator<History> CHRONOLOGICAL = Comparator
            .comparing(History::getDate, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(History::getIdHistory, Comparator.nullsLast(Comparator.naturalOrder()));

    private HistoryOrder() {
    }
}
