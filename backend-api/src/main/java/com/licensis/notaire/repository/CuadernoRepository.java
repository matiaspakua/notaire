package com.licensis.notaire.repository;

import com.licensis.notaire.negocio.Cuaderno;
import com.licensis.notaire.negocio.Persona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CuadernoRepository extends JpaRepository<Cuaderno, Integer> {

    List<Cuaderno> findByAnioAndFkIdPersonaEscribano(int anio, Persona escribano);

    boolean existsByNumeroAndAnioAndFkIdPersonaEscribano(int numero, int anio, Persona escribano);
}
