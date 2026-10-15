package com.licensis.notaire.application.usecase.catalog;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

/**
 * Shared write-lock control flow for catalog management use cases (issue #1722, ADR-030). Each catalog keeps its
 * own domain types, usage ports and exception messages; this helper only owns load → lock → version → save/delete.
 */
public final class CatalogLockSupport {

    private CatalogLockSupport() {}

    @FunctionalInterface
    public interface StaleVersionFactory {
        RuntimeException create(int id, int stored, int received);
    }

    @FunctionalInterface
    public interface IntToBooleanFunction {
        boolean apply(int id);
    }

    /**
     * Update an unused row whose stored version matches the client version.
     *
     * @param change maps the loaded row to the replacement to save
     * @param save persists the changed row
     */
    public static <T> void update(
            int id,
            IntFunction<Optional<T>> findById,
            IntPredicate inUse,
            ToIntFunction<T> versionOf,
            int clientVersion,
            Supplier<? extends RuntimeException> notFound,
            Supplier<? extends RuntimeException> lockedForEdit,
            StaleVersionFactory staleVersion,
            Function<T, T> change,
            java.util.function.Consumer<T> save) {
        T existing = findById.apply(id).orElseThrow(notFound);
        if (inUse.test(id)) {
            throw lockedForEdit.get();
        }
        int stored = versionOf.applyAsInt(existing);
        if (stored != clientVersion) {
            throw staleVersion.create(id, stored, clientVersion);
        }
        save.accept(change.apply(existing));
    }

    /**
     * Delete an unused row. Throws {@code notFound} when the id is absent or when {@code deleteById} reports it was
     * not deleted.
     */
    public static <T> void delete(
            int id,
            IntFunction<Optional<T>> findById,
            IntPredicate inUse,
            Supplier<? extends RuntimeException> notFound,
            Supplier<? extends RuntimeException> cannotDelete,
            IntToBooleanFunction deleteById) {
        findById.apply(id).orElseThrow(notFound);
        if (inUse.test(id)) {
            throw cannotDelete.get();
        }
        if (!deleteById.apply(id)) {
            throw notFound.get();
        }
    }
}
