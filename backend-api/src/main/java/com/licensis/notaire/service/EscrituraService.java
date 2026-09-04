package com.licensis.notaire.service;

import com.licensis.notaire.exception.NumeroEscrituraDuplicadoException;
import com.licensis.notaire.exception.SaltoNumeracionSinJustificarException;
import com.licensis.notaire.negocio.Escritura;
import com.licensis.notaire.negocio.Folio;
import com.licensis.notaire.negocio.Persona;
import com.licensis.notaire.repository.EscrituraRepository;
import com.licensis.notaire.repository.FolioRepository;
import com.licensis.notaire.repository.PersonaRepository;
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
public class EscrituraService {

    private static final Logger logger = LoggerFactory.getLogger(EscrituraService.class);

    private final EscrituraRepository escrituraRepository;
    private final PersonaRepository personaRepository;
    private final FolioRepository folioRepository;
    private final NumeracionEscrituraService numeracionEscrituraService;

    public EscrituraService(EscrituraRepository escrituraRepository, PersonaRepository personaRepository,
            FolioRepository folioRepository, NumeracionEscrituraService numeracionEscrituraService) {
        this.escrituraRepository = escrituraRepository;
        this.personaRepository = personaRepository;
        this.folioRepository = folioRepository;
        this.numeracionEscrituraService = numeracionEscrituraService;
    }

    @Transactional(readOnly = true)
    public Page<Escritura> findAllPaged(Pageable pageable) {
        return escrituraRepository.findAll(pageable);
    }

    public List<Escritura> findAll() {
        logger.debug("Finding all escrituras");
        return escrituraRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Escritura> findById(Integer id) {
        logger.debug("Finding escritura by id: {}", id);
        return escrituraRepository.findById(id);
    }

    public Escritura save(Escritura entity) {
        logger.info("Saving escritura with numero: {}", entity.getNumero());
        validarNumeracion(entity);
        return escrituraRepository.save(entity);
    }

    private void validarNumeracion(Escritura entity) {
        resolveFolioParaNumeracion(entity).ifPresent(folio -> {
            Persona escribano = folio.getFkIdPersonaEscribano();
            if (escribano == null || escribano.getIdPersona() == null) {
                return;
            }
            boolean esAuxiliar = folio.getFkIdTipoFolio() != null && folio.getFkIdTipoFolio().isEsAuxiliar();
            ResultadoValidacionNumeracion resultado = numeracionEscrituraService.validar(
                    entity.getNumero(), escribano, folio.getAnio(), esAuxiliar,
                    entity.getObservaciones(), entity.getIdEscritura());

            if (resultado == ResultadoValidacionNumeracion.DUPLICADO) {
                throw new NumeroEscrituraDuplicadoException(
                        "El número " + entity.getNumero() + " ya fue utilizado en el protocolo "
                                + (esAuxiliar ? "auxiliar" : "principal") + " del año " + folio.getAnio());
            }
            if (resultado == ResultadoValidacionNumeracion.SALTO_SIN_JUSTIFICAR) {
                throw new SaltoNumeracionSinJustificarException(
                        "El número " + entity.getNumero() + " deja un salto en la numeración correlativa; "
                                + "debe indicar una justificación en observaciones");
            }
        });
    }

    private Optional<Folio> resolveFolioParaNumeracion(Escritura entity) {
        if (entity.getIdFolio() != null) {
            return folioRepository.findById(entity.getIdFolio());
        }
        if (entity.getIdEscritura() != null) {
            return folioRepository.findByFkIdEscrituraIdEscritura(entity.getIdEscritura());
        }
        return Optional.empty();
    }

    public void deleteById(Integer id) {
        logger.info("Deleting escritura with id: {}", id);
        escrituraRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Persona> findEscribanosDisponibles() {
        logger.debug("Finding all available escribanos");
        return personaRepository.findAllEscribanos();
    }

    @Transactional(readOnly = true)
    public List<Escritura> buscarPorNumero(Integer numero) {
        logger.debug("Searching escrituras by numero: {}", numero);
        if (numero == null) {
            return escrituraRepository.findAll();
        }
        return escrituraRepository.findByNumero(numero)
                .map(List::of)
                .orElse(List.of());
    }
}
