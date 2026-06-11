package com.licensis.notaire.service;

import com.licensis.notaire.dto.DtoGestionSummary;
import com.licensis.notaire.negocio.GestionDeEscritura;
import com.licensis.notaire.repository.GestionDeEscrituraRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class GestionQueryService {

    private final GestionDeEscrituraRepository repository;

    public GestionQueryService(GestionDeEscrituraRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Page<DtoGestionSummary> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(GestionQueryService::toSummary);
    }

    @Transactional(readOnly = true)
    public Optional<DtoGestionSummary> findById(Integer id) {
        return repository.findById(id).map(GestionQueryService::toSummary);
    }

    @Transactional(readOnly = true)
    public Optional<DtoGestionSummary> findByNumero(int numero) {
        return repository.findByNumero(numero).map(GestionQueryService::toSummary);
    }

    private static DtoGestionSummary toSummary(GestionDeEscritura gestion) {
        return new DtoGestionSummary(
                gestion.getIdGestion(),
                gestion.getNumero(),
                gestion.getEncabezado(),
                gestion.getFechaInicio(),
                gestion.getFkIdEstadoDeGestion() != null ? gestion.getFkIdEstadoDeGestion().getNombre() : null,
                gestion.getTramiteList() != null ? gestion.getTramiteList().size() : 0);
    }
}
