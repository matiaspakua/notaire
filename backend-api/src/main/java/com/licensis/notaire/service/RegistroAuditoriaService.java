package com.licensis.notaire.service;

import com.licensis.notaire.negocio.RegistroAuditoria;
import com.licensis.notaire.negocio.Usuario;
import com.licensis.notaire.repository.RegistroAuditoriaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class RegistroAuditoriaService {

    private static final Logger logger = LoggerFactory.getLogger(RegistroAuditoriaService.class);

    private final RegistroAuditoriaRepository repository;

    public RegistroAuditoriaService(RegistroAuditoriaRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<RegistroAuditoria> findAll() {
        logger.debug("Finding all RegistroAuditoria");
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<RegistroAuditoria> findById(Integer id) {
        logger.debug("Finding RegistroAuditoria by id: {}", id);
        return repository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<RegistroAuditoria> findByUsuarioId(Integer idUsuario) {
        logger.debug("Finding RegistroAuditoria by usuario id: {}", idUsuario);
        return repository.findByFkIdUsuarioIdUsuario(idUsuario);
    }

    @Transactional
    public RegistroAuditoria save(RegistroAuditoria registroAuditoria) {
        logger.debug("Saving RegistroAuditoria: {}", registroAuditoria);
        return repository.save(registroAuditoria);
    }

    @Transactional
    public void deleteById(Integer id) {
        logger.debug("Deleting RegistroAuditoria by id: {}", id);
        repository.deleteById(id);
    }
}
