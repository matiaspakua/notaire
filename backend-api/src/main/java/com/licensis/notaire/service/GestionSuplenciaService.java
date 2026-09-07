package com.licensis.notaire.service;

import com.licensis.notaire.negocio.Persona;
import com.licensis.notaire.negocio.Suplencia;
import com.licensis.notaire.repository.SuplenciaRepository;
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
public class GestionSuplenciaService {

    private final SuplenciaRepository suplenciaRepository;

    public GestionSuplenciaService(SuplenciaRepository suplenciaRepository) {
        this.suplenciaRepository = suplenciaRepository;
    }

    /**
     * Escribano finalmente asignado a la gestión, junto con la suplencia que
     * motivó la redirección ({@code null} cuando no hubo redirección).
     */
    public record EscribanoAsignado(Persona escribano, Suplencia suplenciaAplicada) { }

    @Transactional(readOnly = true)
    public EscribanoAsignado resolverEscribano(Persona escribanoSolicitado, Date fecha) {
        List<Suplencia> suplenciasActivas = suplenciaRepository
                .findByFkIdSuplantadoIdPersonaAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual(
                        escribanoSolicitado.getIdPersona(), fecha, fecha);
        return suplenciasActivas.stream()
                .findFirst()
                .map(suplencia -> new EscribanoAsignado(suplencia.getFkIdSuplente(), suplencia))
                .orElseGet(() -> new EscribanoAsignado(escribanoSolicitado, null));
    }

    public String observacionRedireccion(Persona escribanoSolicitado, Persona suplente) {
        return "Gestión redirigida por suplencia activa: escribano solicitado %s %s, asignada al suplente %s %s"
                .formatted(escribanoSolicitado.getNombre(), escribanoSolicitado.getApellido(),
                        suplente.getNombre(), suplente.getApellido());
    }
}
