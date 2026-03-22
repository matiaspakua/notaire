package com.licensis.notaire.service;

import com.licensis.notaire.negocio.Escritura;
import com.licensis.notaire.negocio.Persona;
import com.licensis.notaire.repository.EscrituraRepository;
import com.licensis.notaire.repository.PersonaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class EscrituraService {

    private static final Logger logger = LoggerFactory.getLogger(EscrituraService.class);

    private final EscrituraRepository escrituraRepository;
    private final PersonaRepository personaRepository;

    public EscrituraService(EscrituraRepository escrituraRepository, PersonaRepository personaRepository) {
        this.escrituraRepository = escrituraRepository;
        this.personaRepository = personaRepository;
    }

    @Transactional(readOnly = true)
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
        return escrituraRepository.save(entity);
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
