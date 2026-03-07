package com.licensis.notaire.repository;

import com.licensis.notaire.negocio.Usuario;
import com.licensis.notaire.negocio.Persona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByNombreUsuario(String nombreUsuario);

    Optional<Usuario> findByFkIdPersona(Persona persona);

    Optional<Usuario> findByFkIdPersonaIdPersona(Integer idPersona);

    List<Usuario> findByEstado(String estado);

    boolean existsByNombreUsuario(String nombreUsuario);
}
