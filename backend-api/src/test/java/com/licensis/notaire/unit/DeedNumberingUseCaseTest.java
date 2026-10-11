package com.licensis.notaire.unit;

import com.licensis.notaire.application.port.out.deed.DeedNumberingPort;
import com.licensis.notaire.application.usecase.deed.DeedNumberingUseCaseImpl;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.domain.deed.DeedNumberingValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DeedNumberingUseCaseImpl Unit Tests (CU86)")
class DeedNumberingUseCaseTest {

    private DeedNumberingUseCaseImpl deedNumberingUseCase;
    private InMemoryDeedNumberingPort inMemoryPort;

    @BeforeEach
    void setUp() {
        inMemoryPort = new InMemoryDeedNumberingPort();
        deedNumberingUseCase = new DeedNumberingUseCaseImpl(inMemoryPort);
    }

    @Test
    @DisplayName("Should start the sequence at one when no deed exists yet in scope")
    void shouldStartSequenceAtOneWhenScopeIsEmpty() {
        Person notary = createNotary(1);

        int next = deedNumberingUseCase.calculateNextSequenceNumber(notary, 2026, false);

        assertThat(next).isEqualTo(1);
    }

    @Test
    @DisplayName("Should accept a number matching the expected sequence")
    void shouldAcceptNumberMatchingExpectedSequence() {
        Person notary = createNotary(1);
        inMemoryPort.recordMaxNumber(1, 2026, false, null, 5);

        DeedNumberingValidationResult result = deedNumberingUseCase.validate(
                6, notary, 2026, false, null, null);

        assertThat(result).isEqualTo(DeedNumberingValidationResult.OK);
    }

    @Test
    @DisplayName("Should reject a number already used within the same scope")
    void shouldRejectDuplicateNumber() {
        Person notary = createNotary(1);
        inMemoryPort.recordDuplicate(5, 1, 2026, false, null);

        DeedNumberingValidationResult result = deedNumberingUseCase.validate(
                5, notary, 2026, false, null, null);

        assertThat(result).isEqualTo(DeedNumberingValidationResult.DUPLICATE);
    }

    @Test
    @DisplayName("Should require a justification when the number leaves a gap")
    void shouldRequireJustificationForGap() {
        Person notary = createNotary(1);
        inMemoryPort.recordMaxNumber(1, 2026, false, null, 5);

        DeedNumberingValidationResult result = deedNumberingUseCase.validate(
                9, notary, 2026, false, null, null);

        assertThat(result).isEqualTo(DeedNumberingValidationResult.SKIP_UNJUSTIFIED);
    }

    @Test
    @DisplayName("Should accept a gap when a justification is provided")
    void shouldAcceptGapWithJustification() {
        Person notary = createNotary(1);
        inMemoryPort.recordMaxNumber(1, 2026, false, null, 5);

        DeedNumberingValidationResult result = deedNumberingUseCase.validate(
                9, notary, 2026, false, "Voided deed No. 6 to 8", null);

        assertThat(result).isEqualTo(DeedNumberingValidationResult.SKIP_JUSTIFIED);
    }

    @Test
    @DisplayName("Should keep Protocolo Auxiliar numbering independent of Protocolo Principal")
    void shouldKeepAuxiliaryNumberingIndependentFromPrincipal() {
        Person notary = createNotary(1);
        inMemoryPort.recordMaxNumber(1, 2026, true, null, 2);
        inMemoryPort.recordMaxNumber(1, 2026, false, null, 40);

        int nextAuxiliary = deedNumberingUseCase.calculateNextSequenceNumber(notary, 2026, true);
        int nextPrincipal = deedNumberingUseCase.calculateNextSequenceNumber(notary, 2026, false);

        assertThat(nextAuxiliary).isEqualTo(3);
        assertThat(nextPrincipal).isEqualTo(41);
    }

    private Person createNotary(int notaryId) {
        Person notary = new Person();
        notary.setPersonId(notaryId);
        notary.setNotaryRegistrationNumber(100);
        return notary;
    }

    /**
     * In-memory fake implementation of DeedNumberingPort for testing.
     */
    static class InMemoryDeedNumberingPort implements DeedNumberingPort {
        private final Map<String, Integer> maxNumbers = new HashMap<>();
        private final Map<String, Boolean> duplicates = new HashMap<>();

        void recordMaxNumber(Integer notaryId, int year, boolean auxiliary, Integer excludedDeedId,
                int maxNumber) {
            String key = makeKey(notaryId, year, auxiliary, excludedDeedId);
            maxNumbers.put(key, maxNumber);
        }

        void recordDuplicate(int number, Integer notaryId, int year, boolean auxiliary,
                Integer excludedDeedId) {
            String key = makeKey(number, notaryId, year, auxiliary, excludedDeedId);
            duplicates.put(key, true);
        }

        private String makeKey(Object... parts) {
            StringBuilder sb = new StringBuilder();
            for (Object part : parts) {
                if (sb.length() > 0) {
                    sb.append("|");
                }
                sb.append(part);
            }
            return sb.toString();
        }

        @Override
        public Optional<Integer> findMaxNumberDeedByNotaryYearAndType(
                Integer notaryId, int year, boolean auxiliary, Integer excludedDeedId) {
            String key = makeKey(notaryId, year, auxiliary, excludedDeedId);
            return Optional.ofNullable(maxNumbers.get(key));
        }

        @Override
        public boolean existsNumberDeedByNotaryYearAndType(
                int number, Integer notaryId, int year, boolean auxiliary, Integer excludedDeedId) {
            String key = makeKey(number, notaryId, year, auxiliary, excludedDeedId);
            return duplicates.getOrDefault(key, false);
        }
    }
}
