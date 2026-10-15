package com.licensis.notaire.adapter.out.persistence.folio;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.licensis.notaire.application.port.out.folio.FolioCatalogPort;
import com.licensis.notaire.business.Deed;
import com.licensis.notaire.business.FolioType;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.domain.folio.Folio;
import com.licensis.notaire.domain.folio.FolioInvalidException;
import com.licensis.notaire.domain.folio.FolioNotFoundException;
import com.licensis.notaire.repository.DeedRepository;
import com.licensis.notaire.repository.FolioRepository;
import com.licensis.notaire.repository.FolioTypeRepository;
import com.licensis.notaire.repository.PersonRepository;

/**
 * Maps the legacy {@code business.Folio} entity to the pure domain type for the ports-only controller (issue #1727).
 */
@Component
public class FolioCatalogPersistenceAdapter implements FolioCatalogPort {

    private final FolioRepository folioRepository;
    private final FolioTypeRepository folioTypeRepository;
    private final PersonRepository personRepository;
    private final DeedRepository deedRepository;

    public FolioCatalogPersistenceAdapter(
            FolioRepository folioRepository,
            FolioTypeRepository folioTypeRepository,
            PersonRepository personRepository,
            DeedRepository deedRepository) {
        this.folioRepository = folioRepository;
        this.folioTypeRepository = folioTypeRepository;
        this.personRepository = personRepository;
        this.deedRepository = deedRepository;
    }

    @Override
    public List<Folio> findAll() {
        return folioRepository.findAll().stream().map(FolioCatalogPersistenceAdapter::toDomain).toList();
    }

    @Override
    public List<Folio> findByStatus(String status) {
        return folioRepository.findByStatus(status).stream().map(FolioCatalogPersistenceAdapter::toDomain).toList();
    }

    @Override
    public Optional<Folio> findById(int id) {
        return folioRepository.findById(id).map(FolioCatalogPersistenceAdapter::toDomain);
    }

    @Override
    public List<Folio> findAvailableAuxiliaryFolios() {
        return folioRepository.findAvailableAuxiliaryFolios().stream()
                .map(FolioCatalogPersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public Folio save(Folio folio) {
        com.licensis.notaire.business.Folio entity = folio.id() == null
                ? new com.licensis.notaire.business.Folio()
                : folioRepository.findById(folio.id()).orElseThrow(() -> new FolioNotFoundException(folio.id()));
        applyDomain(entity, folio);
        return toDomain(folioRepository.save(entity));
    }

    @Override
    public boolean deleteById(int id) {
        Optional<com.licensis.notaire.business.Folio> opt = folioRepository.findById(id);
        if (opt.isEmpty()) {
            return false;
        }
        com.licensis.notaire.business.Folio folio = opt.get();
        if (folio.getFkIdFolioType() != null && folio.getFkIdFolioType().getFolioList() != null) {
            folio.getFkIdFolioType().getFolioList().remove(folio);
        }
        folioRepository.delete(folio);
        return true;
    }

    private void applyDomain(com.licensis.notaire.business.Folio entity, Folio folio) {
        entity.setNumber(folio.number());
        entity.setYear(folio.year());
        entity.setStatus(folio.status());
        entity.setNotes(folio.notes());
        if (folio.folioTypeId() != null) {
            FolioType type = folioTypeRepository
                    .findById(folio.folioTypeId())
                    .orElseThrow(() -> new FolioInvalidException("Folio type not found: " + folio.folioTypeId()));
            entity.setFkIdFolioType(type);
        }
        if (folio.notaryId() != null) {
            Person notary = personRepository
                    .findById(folio.notaryId())
                    .orElseThrow(() -> new FolioInvalidException("Notary not found: " + folio.notaryId()));
            entity.setFkIdNotaryPerson(notary);
        }
        if (folio.deedId() != null) {
            Deed deed = deedRepository
                    .findById(folio.deedId())
                    .orElseThrow(() -> new FolioInvalidException("Deed not found: " + folio.deedId()));
            entity.setFkIdDeed(deed);
        }
    }

    private static Folio toDomain(com.licensis.notaire.business.Folio entity) {
        FolioType type = entity.getFkIdFolioType();
        Person notary = entity.getFkIdNotaryPerson();
        Deed deed = entity.getFkIdDeed();
        return Folio.restore(
                entity.getIdFolio(),
                entity.getNumber(),
                entity.getYear(),
                entity.getStatus(),
                entity.getNotes(),
                type == null ? null : type.getIdFolioType(),
                type == null ? null : type.getName(),
                type == null ? null : type.getNotes(),
                type != null && Boolean.TRUE.equals(type.getEnabled()),
                type != null && type.isIsAuxiliary(),
                notary == null ? null : notary.getPersonId(),
                notary == null ? null : notary.getFirstName(),
                notary == null ? null : notary.getLastName(),
                notary == null ? null : notary.getIdentificationNumber(),
                notary == null ? null : notary.getNotaryRegistrationNumber(),
                deed == null ? null : deed.getIdDeed(),
                deed == null ? null : Integer.valueOf(deed.getNumber()),
                deed == null ? null : deed.getStatus(),
                entity.getVersion());
    }
}
