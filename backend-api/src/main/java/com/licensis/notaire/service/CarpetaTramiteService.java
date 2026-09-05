package com.licensis.notaire.service;

import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.negocio.CarpetaTramite;
import com.licensis.notaire.negocio.Tramite;
import com.licensis.notaire.repository.CarpetaTramiteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * CU85 - Administrar Carpetas de Trámite: genera automáticamente una carpeta
 * por cada trámite dado de alta y administra su ciclo de vida
 * activa/espera/archivada.
 */
@Service
@Transactional
public class CarpetaTramiteService {

    static final String ESTADO_ACTIVA = "Activa";
    static final String ESTADO_ESPERA = "Espera";

    private static final Logger log = LoggerFactory.getLogger(CarpetaTramiteService.class);

    private final CarpetaTramiteRepository carpetaTramiteRepository;

    public CarpetaTramiteService(CarpetaTramiteRepository carpetaTramiteRepository) {
        this.carpetaTramiteRepository = carpetaTramiteRepository;
    }

    public CarpetaTramite generarCarpetaParaTramite(Tramite tramite) {
        CarpetaTramite carpeta = new CarpetaTramite();
        carpeta.setNumero(calcularSiguienteNumero());
        carpeta.setEstado(ESTADO_ACTIVA);
        carpeta.setFkIdGestion(tramite.getFkIdGestion());
        carpeta.setFkIdTramite(tramite);
        CarpetaTramite guardada = carpetaTramiteRepository.save(carpeta);
        log.info("Carpeta {} generada para trámite {} de gestión {}", guardada.getNumero(),
                tramite.getIdTramite(), tramite.getFkIdGestion().getIdGestion());
        return guardada;
    }

    private int calcularSiguienteNumero() {
        return carpetaTramiteRepository.findTopByOrderByNumeroDesc()
                .map(c -> c.getNumero() + 1)
                .orElse(1);
    }

    @Transactional(readOnly = true)
    public Optional<CarpetaTramite> findById(Integer id) {
        return carpetaTramiteRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<CarpetaTramite> findByTramite(Integer idTramite) {
        return carpetaTramiteRepository.findByFkIdTramiteIdTramite(idTramite);
    }

    @Transactional(readOnly = true)
    public List<CarpetaTramite> findByGestion(Integer idGestion) {
        return carpetaTramiteRepository.findByFkIdGestionIdGestion(idGestion);
    }

    public CarpetaTramite ponerEnEspera(Integer idCarpeta, String motivo) {
        if (motivo == null || motivo.isBlank()) {
            throw new BusinessValidationException(
                    "El motivo es obligatorio para poner una carpeta en espera");
        }
        CarpetaTramite carpeta = carpetaTramiteRepository.findById(idCarpeta)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la carpeta con ID: " + idCarpeta));
        carpeta.setEstado(ESTADO_ESPERA);
        carpeta.setMotivoEspera(motivo);
        return carpetaTramiteRepository.save(carpeta);
    }
}
