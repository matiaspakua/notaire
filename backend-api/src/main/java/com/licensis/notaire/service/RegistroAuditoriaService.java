package com.licensis.notaire.service;

import com.licensis.notaire.dto.DtoPersona;
import com.licensis.notaire.dto.DtoRegistroAuditoria;
import com.licensis.notaire.dto.DtoUsuario;
import com.licensis.notaire.negocio.Persona;
import com.licensis.notaire.negocio.RegistroAuditoria;
import com.licensis.notaire.negocio.Usuario;
import com.licensis.notaire.repository.RegistroAuditoriaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public List<DtoRegistroAuditoria> findAllAsDto() {
        logger.debug("Finding all RegistroAuditoria as DTO");
        return repository.findAllWithUsuario().stream()
                .map(RegistroAuditoriaService::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<RegistroAuditoria> findAll(Pageable pageable) {
        logger.debug("Finding page of RegistroAuditoria: {}", pageable);
        return repository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<DtoRegistroAuditoria> findAllAsDto(Pageable pageable) {
        logger.debug("Finding page of RegistroAuditoria as DTO: {}", pageable);
        return repository.findAll(pageable).map(RegistroAuditoriaService::toDto);
    }

    @Transactional(readOnly = true)
    public Page<RegistroAuditoria> findByModulo(String modulo, Pageable pageable) {
        logger.debug("Finding RegistroAuditoria by modulo={} page={}", modulo, pageable);
        return repository.findByModulo(modulo, pageable);
    }

    @Transactional(readOnly = true)
    public Page<DtoRegistroAuditoria> findByModuloAsDto(String modulo, Pageable pageable) {
        logger.debug("Finding RegistroAuditoria by modulo={} page={} as DTO", modulo, pageable);
        return repository.findByModulo(modulo, pageable).map(RegistroAuditoriaService::toDto);
    }

    @Transactional(readOnly = true)
    public Page<RegistroAuditoria> findByUsuarioId(Integer idUsuario, Pageable pageable) {
        logger.debug("Finding RegistroAuditoria by usuarioId={} page={}", idUsuario, pageable);
        return repository.findByFkIdUsuarioIdUsuario(idUsuario, pageable);
    }

    @Transactional(readOnly = true)
    public Page<DtoRegistroAuditoria> findByUsuarioIdAsDto(Integer idUsuario, Pageable pageable) {
        logger.debug("Finding RegistroAuditoria by usuarioId={} page={} as DTO", idUsuario, pageable);
        return repository.findByFkIdUsuarioIdUsuario(idUsuario, pageable)
                .map(RegistroAuditoriaService::toDto);
    }

    @Transactional(readOnly = true)
    public Optional<RegistroAuditoria> findById(Integer id) {
        logger.debug("Finding RegistroAuditoria by id: {}", id);
        return repository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<DtoRegistroAuditoria> findByIdAsDto(Integer id) {
        logger.debug("Finding RegistroAuditoria by id as DTO: {}", id);
        return repository.findByIdWithUsuario(id).map(RegistroAuditoriaService::toDto);
    }

    @Transactional(readOnly = true)
    public List<RegistroAuditoria> findByUsuarioId(Integer idUsuario) {
        logger.debug("Finding RegistroAuditoria by usuario id: {}", idUsuario);
        return repository.findByFkIdUsuarioIdUsuario(idUsuario);
    }

    @Transactional(readOnly = true)
    public List<DtoRegistroAuditoria> findByUsuarioIdAsDto(Integer idUsuario) {
        logger.debug("Finding RegistroAuditoria by usuario id as DTO: {}", idUsuario);
        return repository.findByUsuarioIdWithUsuario(idUsuario).stream()
                .map(RegistroAuditoriaService::toDto)
                .toList();
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

    /**
     * Maps a {@link RegistroAuditoria} entity to its DTO inside the current
     * transaction so that the LAZY {@code fkIdUsuario} association can be
     * dereferenced safely.
     */
    public static DtoRegistroAuditoria toDto(RegistroAuditoria entity) {
        DtoRegistroAuditoria dto = new DtoRegistroAuditoria();
        dto.setIdRegistroAuditoria(entity.getIdRegistroAuditoria());
        dto.setDetalleOperacion(entity.getDetalleOperacion());
        dto.setModulo(entity.getModulo());
        dto.setFecha(entity.getFecha());

        Usuario usuario = entity.getFkIdUsuario();
        if (usuario != null) {
            DtoUsuario dtoUsuario = new DtoUsuario();
            dtoUsuario.setIdUsuario(usuario.getIdUsuario());
            dtoUsuario.setNombre(usuario.getNombre());
            dtoUsuario.setTipo(usuario.getTipo());
            dtoUsuario.setEstado(usuario.getEstado());

            Persona persona = usuario.getFkIdPersona();
            if (persona != null) {
                DtoPersona dtoPersona = new DtoPersona();
                dtoPersona.setIdPersona(persona.getIdPersona());
                dtoPersona.setNombre(persona.getNombre());
                dtoPersona.setApellido(persona.getApellido());
                dtoUsuario.setPersonas(dtoPersona);
            }

            dto.setUsuarios(dtoUsuario);
        }
        return dto;
    }
}
