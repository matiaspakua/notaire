package com.licensis.notaire.service;

import com.licensis.notaire.exception.NumberDeedDuplicadoException;
import com.licensis.notaire.exception.SaltoNumeracionSinJustificarException;
import com.licensis.notaire.business.Deed;
import com.licensis.notaire.business.Folio;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.repository.DeedRepository;
import com.licensis.notaire.repository.FolioRepository;
import com.licensis.notaire.repository.PersonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@Transactional
public class DeedService {

    private static final Logger logger = LoggerFactory.getLogger(DeedService.class);

    private final DeedRepository deedRepository;
    private final PersonRepository personRepository;
    private final FolioRepository folioRepository;
    private final NumeracionDeedService numeracionDeedService;

    public DeedService(DeedRepository deedRepository, PersonRepository personRepository,
            FolioRepository folioRepository, NumeracionDeedService numeracionDeedService) {
        this.deedRepository = deedRepository;
        this.personRepository = personRepository;
        this.folioRepository = folioRepository;
        this.numeracionDeedService = numeracionDeedService;
    }

    @Transactional(readOnly = true)
    public Page<Deed> findAllPaged(Pageable pageable) {
        return deedRepository.findAll(pageable);
    }

    public List<Deed> findAll() {
        logger.debug("Finding all escrituras");
        return deedRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Deed> findById(Integer id) {
        logger.debug("Finding escritura by id: {}", id);
        return deedRepository.findById(id);
    }

    public Deed save(Deed entity) {
        logger.info("Saving escritura with numero: {}", entity.getNumber());
        validarNumeracion(entity);
        return deedRepository.save(entity);
    }

    private void validarNumeracion(Deed entity) {
        resolveFolioParaNumeracion(entity).ifPresent(folio -> {
            Person notary = folio.getFkIdNotaryPerson();
            if (notary == null || notary.getPersonId() == null) {
                return;
            }
            boolean isAuxiliary = folio.getFkIdFolioType() != null && folio.getFkIdFolioType().isIsAuxiliary();
            ResultadoValidacionNumeracion resultado = numeracionDeedService.validar(
                    entity.getNumber(), notary, folio.getYear(), isAuxiliary,
                    entity.getNotes(), entity.getIdDeed());

            if (resultado == ResultadoValidacionNumeracion.DUPLICADO) {
                throw new NumberDeedDuplicadoException(
                        "El número " + entity.getNumber() + " ya fue utilizado en el protocolo "
                                + (isAuxiliary ? "auxiliar" : "principal") + " del año " + folio.getYear());
            }
            if (resultado == ResultadoValidacionNumeracion.SALTO_SIN_JUSTIFICAR) {
                throw new SaltoNumeracionSinJustificarException(
                        "El número " + entity.getNumber() + " deja un salto en la numeración correlativa; "
                                + "debe indicar una justificación en observaciones");
            }
        });
    }

    private Optional<Folio> resolveFolioParaNumeracion(Deed entity) {
        if (entity.getIdFolio() != null) {
            return folioRepository.findById(entity.getIdFolio());
        }
        if (entity.getIdDeed() != null) {
            return folioRepository.findByFkIdDeedIdDeed(entity.getIdDeed());
        }
        return Optional.empty();
    }

    public void deleteById(Integer id) {
        logger.info("Deleting escritura with id: {}", id);
        deedRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Person> findEscribanosDisponibles() {
        logger.debug("Finding all available escribanos");
        return personRepository.findAllEscribanos();
    }

    @Transactional(readOnly = true)
    public List<Deed> searchPorNumber(Integer number) {
        logger.debug("Searching escrituras by numero: {}", number);
        if (number == null) {
            return deedRepository.findAll();
        }
        return deedRepository.findByNumber(number)
                .map(List::of)
                .orElse(List.of());
    }
}
