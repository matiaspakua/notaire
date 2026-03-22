package com.licensis.notaire.service;

import com.licensis.notaire.negocio.Persona;
import com.licensis.notaire.repository.PersonaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PersonaService {

    private static final Logger logger = LoggerFactory.getLogger(PersonaService.class);

    private final PersonaRepository personaRepository;

    public PersonaService(PersonaRepository personaRepository) {
        this.personaRepository = personaRepository;
    }

    @Transactional(readOnly = true)
    public List<Persona> findAll() {
        logger.debug("Finding all personas");
        return personaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Persona> findById(Integer id) {
        logger.debug("Finding persona by id: {}", id);
        return personaRepository.findById(id);
    }

    public Persona save(Persona entity) {
        logger.info("Saving persona: {} {}", entity.getNombre(), entity.getApellido());
        return personaRepository.save(entity);
    }

    public void deleteById(Integer id) {
        logger.info("Deleting persona with id: {}", id);
        personaRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Persona> buscar(String nombre, String apellido, String numeroIdentificacion,
                                 Integer idTipoIdentificacion, Boolean esCliente) {
        logger.debug("Searching personas with filters - nombre: {}, apellido: {}, numeroIdentificacion: {}, "
                + "idTipoIdentificacion: {}, esCliente: {}", nombre, apellido, numeroIdentificacion,
                idTipoIdentificacion, esCliente);

        List<Persona> resultados = new ArrayList<>();

        if (numeroIdentificacion != null && !numeroIdentificacion.isBlank()) {
            Optional<Persona> persona = personaRepository.findByNumeroIdentificacion(numeroIdentificacion);
            persona.ifPresent(resultados::add);
            return resultados;
        }

        if (nombre != null && !nombre.isBlank() && apellido != null && !apellido.isBlank()) {
            return personaRepository.findByNombreAndApellidoContainingIgnoreCase(nombre, apellido);
        }

        if (nombre != null && !nombre.isBlank()) {
            resultados.addAll(personaRepository.findByNombreContainingIgnoreCase(nombre));
        }

        if (apellido != null && !apellido.isBlank()) {
            resultados.addAll(personaRepository.findByApellidoContainingIgnoreCase(apellido));
        }

        if (idTipoIdentificacion != null) {
            resultados.addAll(personaRepository.findByFkIdTipoIdentificacionIdTipoIdentificacion(idTipoIdentificacion));
        }

        if (esCliente != null) {
            resultados.addAll(personaRepository.findByEsCliente(esCliente));
        }

        return resultados.stream().distinct().toList();
    }
}
