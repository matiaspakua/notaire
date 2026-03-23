package com.licensis.notaire.repository;

import com.licensis.notaire.negocio.Copia;
import com.licensis.notaire.negocio.Testimonio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CopiaRepository extends JpaRepository<Copia, Integer> {

    List<Copia> findByFkIdTestimonio(Testimonio testimonio);

    List<Copia> findByFkIdTestimonioIdTestimonio(Integer idTestimonio);

    List<Copia> findByFkIdPersonaIdPersona(Integer idPersona);

    List<Copia> findByNumero(int numero);
}
