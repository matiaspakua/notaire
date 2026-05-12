package com.licensis.notaire.repository;

import com.licensis.notaire.negocio.RegistroAuditoria;
import com.licensis.notaire.negocio.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegistroAuditoriaRepository extends JpaRepository<RegistroAuditoria, Integer> {

    List<RegistroAuditoria> findByFkIdUsuario(Usuario usuario);

    List<RegistroAuditoria> findByFkIdUsuarioIdUsuario(Integer idUsuario);

    Page<RegistroAuditoria> findByFkIdUsuarioIdUsuario(Integer idUsuario, Pageable pageable);

    Page<RegistroAuditoria> findByModulo(String modulo, Pageable pageable);

    @Query("SELECT r FROM RegistroAuditoria r LEFT JOIN FETCH r.fkIdUsuario u "
            + "LEFT JOIN FETCH u.fkIdPersona")
    List<RegistroAuditoria> findAllWithUsuario();

    @Query("SELECT r FROM RegistroAuditoria r LEFT JOIN FETCH r.fkIdUsuario u "
            + "LEFT JOIN FETCH u.fkIdPersona WHERE r.idRegistroAuditoria = :id")
    Optional<RegistroAuditoria> findByIdWithUsuario(@Param("id") Integer id);

    @Query("SELECT r FROM RegistroAuditoria r LEFT JOIN FETCH r.fkIdUsuario u "
            + "LEFT JOIN FETCH u.fkIdPersona WHERE u.idUsuario = :idUsuario")
    List<RegistroAuditoria> findByUsuarioIdWithUsuario(@Param("idUsuario") Integer idUsuario);
}
