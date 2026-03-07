package com.licensis.notaire.repository;

import com.licensis.notaire.negocio.Persona;
import com.licensis.notaire.negocio.TipoIdentificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PersonaRepository extends JpaRepository<Persona, Integer> {

    Optional<Persona> findByNumeroIdentificacion(String numeroIdentificacion);

    List<Persona> findByEsCliente(boolean esCliente);

    List<Persona> findBySexo(String sexo);

    @Query("SELECT p FROM Persona p WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<Persona> findByNombreContainingIgnoreCase(@Param("nombre") String nombre);

    @Query("SELECT p FROM Persona p WHERE LOWER(p.apellido) LIKE LOWER(CONCAT('%', :apellido, '%'))")
    List<Persona> findByApellidoContainingIgnoreCase(@Param("apellido") String apellido);

    @Query("SELECT p FROM Persona p WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')) AND LOWER(p.apellido) LIKE LOWER(CONCAT('%', :apellido, '%'))")
    List<Persona> findByNombreAndApellidoContainingIgnoreCase(@Param("nombre") String nombre, @Param("apellido") String apellido);

    List<Persona> findByFkIdTipoIdentificacion(TipoIdentificacion tipoIdentificacion);

    List<Persona> findByFkIdTipoIdentificacionIdTipoIdentificacion(Integer idTipoIdentificacion);

    @Query("SELECT p FROM Persona p WHERE p.registroEscribano IS NOT NULL")
    List<Persona> findAllEscribanos();

    @Query("SELECT p FROM Persona p WHERE p.esCliente = true")
    List<Persona> findAllClientes();
}
