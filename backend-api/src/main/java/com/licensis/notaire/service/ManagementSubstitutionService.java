package com.licensis.notaire.service;

import com.licensis.notaire.business.Person;
import com.licensis.notaire.business.Substitution;
import com.licensis.notaire.repository.SubstitutionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * CU22/CU59 - Resuelve el escribano efectivo de una gestión: si el escribano
 * solicitado tiene una {@link Suplencia} activa como suplantado para la fecha
 * dada (RF-89), la gestión se redirige a su suplente en su lugar.
 */
@Service
public class ManagementSubstitutionService {

    private final SubstitutionRepository substitutionRepository;

    public ManagementSubstitutionService(SubstitutionRepository substitutionRepository) {
        this.substitutionRepository = substitutionRepository;
    }

    /**
     * Escribano finalmente asignado a la gestión, junto con la suplencia que
     * motivó la redirección ({@code null} cuando no hubo redirección).
     */
    public record NotaryAsignado(Person notary, Substitution substitutionAplicada) { }

    @Transactional(readOnly = true)
    public NotaryAsignado resolverNotary(Person notarySolicitado, Date date) {
        List<Substitution> suplenciasActivas = substitutionRepository
                .findByFkIdSubstitutedIdPersonAndDateStartLessThanEqualAndDateEndGreaterThanEqual(
                        notarySolicitado.getPersonId(), date, date);
        return suplenciasActivas.stream()
                .findFirst()
                .map(substitution -> new NotaryAsignado(substitution.getFkIdSubstitute(), substitution))
                .orElseGet(() -> new NotaryAsignado(notarySolicitado, null));
    }

    public String observacionRedireccion(Person notarySolicitado, Person suplente) {
        return "Gestión redirigida por suplencia activa: escribano solicitado %s %s, asignada al suplente %s %s"
                .formatted(notarySolicitado.getFirstName(), notarySolicitado.getLastName(),
                        suplente.getFirstName(), suplente.getLastName());
    }
}
