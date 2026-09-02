package com.licensis.notaire.service;

import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.negocio.Escritura;
import com.licensis.notaire.negocio.Folio;
import com.licensis.notaire.repository.EscrituraRepository;
import com.licensis.notaire.repository.FolioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * Gestiona el alta de trámites en Protocolo Auxiliar (CU81).
 */
@Service
@Transactional
public class ProtocoloAuxiliarService {

    private final FolioRepository folioRepository;
    private final EscrituraRepository escrituraRepository;

    public ProtocoloAuxiliarService(FolioRepository folioRepository, EscrituraRepository escrituraRepository) {
        this.folioRepository = folioRepository;
        this.escrituraRepository = escrituraRepository;
    }

    @Transactional(readOnly = true)
    public List<Folio> listarFoliosDisponibles() {
        return folioRepository.findFoliosAuxiliaresDisponibles();
    }

    public int calcularSiguienteNumeroAuxiliar() {
        return folioRepository.findMaxNumeroEscrituraAuxiliar().orElse(0) + 1;
    }

    public Escritura iniciarEscritura(Integer idFolio, String cuerpo, Date fechaEscrituracion) {
        if (idFolio == null) {
            throw new BusinessValidationException("Debe indicar el folio auxiliar donde iniciar la escritura");
        }

        Folio folio = folioRepository.findById(idFolio)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el folio con ID: " + idFolio));

        if (!folio.getFkIdTipoFolio().isEsAuxiliar()) {
            throw new BusinessValidationException("El folio indicado no pertenece a Protocolo Auxiliar");
        }
        if (folio.getFkIdEscritura() != null) {
            throw new BusinessValidationException("El folio indicado ya tiene una escritura asociada");
        }

        Escritura escritura = new Escritura();
        escritura.setNumero(calcularSiguienteNumeroAuxiliar());
        escritura.setCuerpo(cuerpo);
        escritura.setFechaEscrituracion(fechaEscrituracion);
        Escritura guardada = escrituraRepository.save(escritura);

        folio.setFkIdEscritura(guardada);
        folioRepository.save(folio);

        return guardada;
    }
}
