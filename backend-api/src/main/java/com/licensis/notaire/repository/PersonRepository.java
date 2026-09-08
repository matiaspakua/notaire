package com.licensis.notaire.repository;

import com.licensis.notaire.negocio.Person;
import com.licensis.notaire.negocio.TipoIdentificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PersonRepository extends JpaRepository<Person, Integer> {

    Optional<Person> findByNumeroIdentificacion(String numeroIdentificacion);

    List<Person> findByEsCliente(boolean esCliente);

    List<Person> findBySexo(String sexo);

    @Query("SELECT p FROM Person p WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<Person> findByNombreContainingIgnoreCase(@Param("nombre") String nombre);

    @Query("SELECT p FROM Person p WHERE LOWER(p.apellido) LIKE LOWER(CONCAT('%', :apellido, '%'))")
    List<Person> findByApellidoContainingIgnoreCase(@Param("apellido") String apellido);

    @Query("SELECT p FROM Person p WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')) "
            + "AND LOWER(p.apellido) LIKE LOWER(CONCAT('%', :apellido, '%'))")
    List<Person> findByNombreAndApellidoContainingIgnoreCase(@Param("nombre") String nombre,
            @Param("apellido") String apellido);

    List<Person> findByFkIdTipoIdentificacion(TipoIdentificacion tipoIdentificacion);

    List<Person> findByFkIdTipoIdentificacionIdTipoIdentificacion(Integer idTipoIdentificacion);

    @Query("SELECT p FROM Person p WHERE p.registroEscribano IS NOT NULL")
    List<Person> findAllEscribanos();

    @Query("SELECT p FROM Person p WHERE p.esCliente = true")
    List<Person> findAllClientes();
}
