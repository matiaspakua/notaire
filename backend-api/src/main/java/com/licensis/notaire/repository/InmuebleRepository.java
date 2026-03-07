package com.licensis.notaire.repository;

import com.licensis.notaire.negocio.Inmueble;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InmuebleRepository extends JpaRepository<Inmueble, Integer> {

    @Query("SELECT i FROM Inmueble i WHERE i.domicilio LIKE %:domicilio%")
    List<Inmueble> findByDomicilioContaining(@Param("domicilio") String domicilio);

    @Query("SELECT i FROM Inmueble i WHERE i.matricula LIKE %:matricula%")
    List<Inmueble> findByMatriculaContaining(@Param("matricula") String matricula);

    List<Inmueble> findByTipoInmueble(String tipoInmueble);
}
