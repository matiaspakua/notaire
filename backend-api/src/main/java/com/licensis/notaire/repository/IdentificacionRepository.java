package com.licensis.notaire.repository;

import com.licensis.notaire.negocio.Identificacion;
import com.licensis.notaire.negocio.Persona;
import com.licensis.notaire.negocio.TipoIdentificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IdentificacionRepository extends JpaRepository<Identificacion, Integer> {

    List<Identificacion> findByPersona(Persona persona);

    List<Identificacion> findByPersonaIdPersona(Integer idPersona);

    List<Identificacion> findByTipoIdentificacion(TipoIdentificacion tipoIdentificacion);

    List<Identificacion> findByTipoIdentificacionIdTipoIdentificacion(Integer idTipoIdentificacion);

    List<Identificacion> findByNumero(int numero);
}
