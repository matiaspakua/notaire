package com.licensis.notaire.service;

import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.negocio.Cuaderno;
import com.licensis.notaire.negocio.Folio;
import com.licensis.notaire.negocio.Persona;
import com.licensis.notaire.repository.CuadernoRepository;
import com.licensis.notaire.repository.FolioRepository;
import com.licensis.notaire.repository.PersonaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CuadernoService {

    private static final int FOLIOS_POR_CUADERNO = 10;
    private static final String ESTADO_ASIGNADO_A_CUADERNO = "Asignado a cuaderno";
    private static final List<String> ESTADOS_DANADOS = List.of("Errose", "no pasó");

    private static final Logger logger = LoggerFactory.getLogger(CuadernoService.class);

    private final CuadernoRepository cuadernoRepository;
    private final FolioRepository folioRepository;
    private final PersonaRepository personaRepository;

    public CuadernoService(CuadernoRepository cuadernoRepository, FolioRepository folioRepository,
                            PersonaRepository personaRepository) {
        this.cuadernoRepository = cuadernoRepository;
        this.folioRepository = folioRepository;
        this.personaRepository = personaRepository;
    }

    @Transactional(readOnly = true)
    public List<Cuaderno> findAll() {
        return cuadernoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Cuaderno> findById(Integer id) {
        return cuadernoRepository.findById(id);
    }

    public Cuaderno crearCuaderno(List<Integer> idsFolio, Integer idEscribano, int anio, String observaciones) {
        Persona escribano = personaRepository.findById(idEscribano)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la persona escribano con ID: " + idEscribano));

        List<Folio> folios = folioRepository.findAllByIdFolioIn(idsFolio);
        if (folios.size() != idsFolio.size()) {
            throw new ResourceNotFoundException("Uno o más folios indicados no existen");
        }

        validarCantidadFolios(folios);
        List<Folio> foliosOrdenados = folios.stream()
                .sorted(Comparator.comparingInt(Folio::getNumero))
                .toList();
        validarMismoEscribano(foliosOrdenados, escribano);
        validarConsecutividad(foliosOrdenados);
        validarNoAsignados(foliosOrdenados);
        validarJustificacionFoliosDanados(foliosOrdenados, observaciones);

        Cuaderno cuaderno = new Cuaderno();
        cuaderno.setAnio(anio);
        cuaderno.setNumero(calcularSiguienteNumero(anio, escribano));
        cuaderno.setObservaciones(observaciones);
        cuaderno.setFkIdPersonaEscribano(escribano);
        Cuaderno guardado = cuadernoRepository.save(cuaderno);

        marcarFoliosAsignados(foliosOrdenados, guardado);
        logger.info("Cuaderno {}/{} creado para escribano {} con {} folios",
                guardado.getNumero(), guardado.getAnio(), escribano.getIdPersona(), foliosOrdenados.size());
        return guardado;
    }

    public int calcularSiguienteNumero(int anio, Persona escribano) {
        int candidato = cuadernoRepository.findByAnioAndFkIdPersonaEscribano(anio, escribano).size() + 1;
        while (cuadernoRepository.existsByNumeroAndAnioAndFkIdPersonaEscribano(candidato, anio, escribano)) {
            candidato++;
        }
        return candidato;
    }

    public void marcarFoliosAsignados(List<Folio> folios, Cuaderno cuaderno) {
        for (Folio folio : folios) {
            folio.setFkIdCuaderno(cuaderno);
            folio.setEstado(ESTADO_ASIGNADO_A_CUADERNO);
        }
        folioRepository.saveAll(folios);
    }

    private void validarCantidadFolios(List<Folio> folios) {
        if (folios.isEmpty() || folios.size() % FOLIOS_POR_CUADERNO != 0) {
            throw new BusinessValidationException(
                    "La cantidad de folios debe ser un múltiplo exacto de " + FOLIOS_POR_CUADERNO);
        }
    }

    private void validarMismoEscribano(List<Folio> folios, Persona escribano) {
        boolean todosMismoEscribano = folios.stream()
                .allMatch(f -> escribano.getIdPersona().equals(f.getFkIdPersonaEscribano().getIdPersona()));
        if (!todosMismoEscribano) {
            throw new BusinessValidationException("Todos los folios deben pertenecer al mismo registro notarial");
        }
    }

    private void validarConsecutividad(List<Folio> foliosOrdenados) {
        for (int i = 1; i < foliosOrdenados.size(); i++) {
            int anterior = foliosOrdenados.get(i - 1).getNumero();
            int actual = foliosOrdenados.get(i).getNumero();
            if (actual != anterior + 1) {
                throw new BusinessValidationException(
                        "Los folios deben ser estrictamente consecutivos y sin faltantes");
            }
        }
    }

    private void validarNoAsignados(List<Folio> folios) {
        boolean yaAsignado = folios.stream().anyMatch(f -> f.getFkIdCuaderno() != null);
        if (yaAsignado) {
            throw new BusinessValidationException("Uno o más folios ya están asignados a otro cuaderno");
        }
    }

    private void validarJustificacionFoliosDanados(List<Folio> folios, String observaciones) {
        boolean hayFolioDanado = folios.stream().anyMatch(f -> ESTADOS_DANADOS.contains(f.getEstado()));
        if (hayFolioDanado && (observaciones == null || observaciones.isBlank())) {
            throw new BusinessValidationException(
                    "Un folio dañado o anulado en el lote requiere una justificación en observaciones");
        }
    }
}
