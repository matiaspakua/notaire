package com.licensis.notaire.application.usecase.audit;

import com.licensis.notaire.application.port.out.audit.AuditRecordRepositoryPort;
import com.licensis.notaire.dto.DtoPerson;
import com.licensis.notaire.dto.DtoAuditRecord;
import com.licensis.notaire.dto.DtoUser;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.business.AuditRecord;
import com.licensis.notaire.business.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AuditRecordService {

    private static final Logger logger = LoggerFactory.getLogger(AuditRecordService.class);

    private final AuditRecordRepositoryPort repository;

    public AuditRecordService(AuditRecordRepositoryPort repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<AuditRecord> findAll() {
        logger.debug("Finding all AuditRecord");
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public List<DtoAuditRecord> findAllAsDto() {
        logger.debug("Finding all AuditRecord as DTO");
        return repository.findAllWithUser().stream()
                .map(AuditRecordService::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<AuditRecord> findAll(Pageable pageable) {
        logger.debug("Finding page of AuditRecord: {}", pageable);
        return repository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<DtoAuditRecord> findAllAsDto(Pageable pageable) {
        logger.debug("Finding page of AuditRecord as DTO: {}", pageable);
        return repository.findAll(pageable).map(AuditRecordService::toDto);
    }

    @Transactional(readOnly = true)
    public Page<AuditRecord> findByModule(String module, Pageable pageable) {
        logger.debug("Finding AuditRecord by modulo={} page={}", module, pageable);
        return repository.findByModule(module, pageable);
    }

    @Transactional(readOnly = true)
    public Page<DtoAuditRecord> findByModuleAsDto(String module, Pageable pageable) {
        logger.debug("Finding AuditRecord by modulo={} page={} as DTO", module, pageable);
        return repository.findByModule(module, pageable).map(AuditRecordService::toDto);
    }

    @Transactional(readOnly = true)
    public Page<AuditRecord> findByUserId(Integer idUser, Pageable pageable) {
        logger.debug("Finding AuditRecord by usuarioId={} page={}", idUser, pageable);
        return repository.findByFkIdUserIdUser(idUser, pageable);
    }

    @Transactional(readOnly = true)
    public Page<DtoAuditRecord> findByUserIdAsDto(Integer idUser, Pageable pageable) {
        logger.debug("Finding AuditRecord by usuarioId={} page={} as DTO", idUser, pageable);
        return repository.findByFkIdUserIdUser(idUser, pageable)
                .map(AuditRecordService::toDto);
    }

    @Transactional(readOnly = true)
    public Optional<AuditRecord> findById(Integer id) {
        logger.debug("Finding AuditRecord by id: {}", id);
        return repository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<DtoAuditRecord> findByIdAsDto(Integer id) {
        logger.debug("Finding AuditRecord by id as DTO: {}", id);
        return repository.findByIdWithUser(id).map(AuditRecordService::toDto);
    }

    @Transactional(readOnly = true)
    public List<AuditRecord> findByUserId(Integer idUser) {
        logger.debug("Finding AuditRecord by user id: {}", idUser);
        return repository.findByFkIdUserIdUser(idUser);
    }

    @Transactional(readOnly = true)
    public List<DtoAuditRecord> findByUserIdAsDto(Integer idUser) {
        logger.debug("Finding AuditRecord by user id as DTO: {}", idUser);
        return repository.findByUserIdWithUser(idUser).stream()
                .map(AuditRecordService::toDto)
                .toList();
    }

    @Transactional
    public AuditRecord save(AuditRecord auditRecord) {
        logger.debug("Saving AuditRecord: {}", auditRecord);
        return repository.save(auditRecord);
    }

    /**
     * Maps a {@link AuditRecord} entity to its DTO inside the current
     * transaction so that the LAZY {@code fkIdUsuario} association can be
     * dereferenced safely.
     */
    public static DtoAuditRecord toDto(AuditRecord entity) {
        DtoAuditRecord dto = new DtoAuditRecord();
        dto.setIdAuditRecord(entity.getIdAuditRecord());
        dto.setOperationDetail(entity.getOperationDetail());
        dto.setModule(entity.getModule());
        dto.setDate(entity.getDate());

        User user = entity.getFkIdUser();
        if (user != null) {
            DtoUser dtoUser = new DtoUser();
            dtoUser.setIdUser(user.getIdUser());
            dtoUser.setName(user.getName());
            dtoUser.setType(user.getType());
            dtoUser.setStatus(user.getStatus());

            Person person = user.getFkIdPerson();
            if (person != null) {
                DtoPerson dtoPerson = new DtoPerson();
                dtoPerson.setId(person.getPersonId());
                dtoPerson.setFirstName(person.getFirstName());
                dtoPerson.setLastName(person.getLastName());
                dtoUser.setPersons(dtoPerson);
            }

            dto.setUsers(dtoUser);
        }
        return dto;
    }
}
