package com.licensis.notaire.repository;

import com.licensis.notaire.negocio.RegistroAuditoria;
import com.licensis.notaire.negocio.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegistroAuditoriaRepository extends JpaRepository<RegistroAuditoria, Integer> {

    List<RegistroAuditoria> findByFkIdUsuario(Usuario usuario);

    List<RegistroAuditoria> findByFkIdUsuarioIdUsuario(Integer idUsuario);
}
