package com.licensis.notaire.application.usecase.management;

import com.licensis.notaire.business.Person;
import com.licensis.notaire.business.Substitution;
import com.licensis.notaire.repository.SubstitutionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * CU22/CU59 - Resolves the effective notary for a management: if the requested
 * notary has an active {@link Substitution} as the substituted notary for the
 * given date (RF-89 / RF-115), the management is redirected to the substitute.
 */
@Service
public class ManagementSubstitutionService {

    private final SubstitutionRepository substitutionRepository;

    public ManagementSubstitutionService(SubstitutionRepository substitutionRepository) {
        this.substitutionRepository = substitutionRepository;
    }

    /**
     * Notary finally assigned to the management, plus the substitution that
     * caused the redirect ({@code null} when no redirect occurred).
     */
    public record AssignedNotary(Person notary, Substitution appliedSubstitution) { }

    @Transactional(readOnly = true)
    public AssignedNotary resolveNotary(Person requestedNotary, Date date) {
        List<Substitution> activeSubstitutions = substitutionRepository
                .findByFkIdSubstitutedIdPersonAndDateStartLessThanEqualAndDateEndGreaterThanEqual(
                        requestedNotary.getPersonId(), date, date);
        return activeSubstitutions.stream()
                .findFirst()
                .map(substitution -> new AssignedNotary(substitution.getFkIdSubstitute(), substitution))
                .orElseGet(() -> new AssignedNotary(requestedNotary, null));
    }

    public String redirectionNote(Person requestedNotary, Person substitute) {
        return "Management redirected by active substitution: requested notary %s %s, assigned to substitute %s %s"
                .formatted(requestedNotary.getFirstName(), requestedNotary.getLastName(),
                        substitute.getFirstName(), substitute.getLastName());
    }
}
