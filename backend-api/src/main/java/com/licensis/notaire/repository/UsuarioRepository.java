package com.licensis.notaire.repository;

import com.licensis.notaire.negocio.Usuario;
import com.licensis.notaire.negocio.Persona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByNombre(String nombre);

    Optional<Usuario> findByFkIdPersona(Persona persona);

    Optional<Usuario> findByFkIdPersonaIdPersona(Integer idPersona);

    // A persona may be linked to more than one usuario; "findFirst" avoids a
    // NonUniqueResult 500 when several rows match.
    Optional<Usuario> findFirstByFkIdPersonaIdPersona(Integer idPersona);

    List<Usuario> findByEstado(boolean estado);

    boolean existsByNombre(String nombre);
}
