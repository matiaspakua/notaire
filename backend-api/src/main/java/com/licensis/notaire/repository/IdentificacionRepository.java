package com.licensis.notaire.repository;

import com.licensis.notaire.negocio.Identificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IdentificacionRepository extends JpaRepository<Identificacion, Integer> {

    @Query("SELECT i FROM Identificacion i WHERE i.nombre LIKE %:nombre%")
    List<Identificacion> findByNombreContaining(@Param("nombre") String nombre);

    List<Identificacion> findByTipoIdentificacion(String tipoIdentificacion);
}
