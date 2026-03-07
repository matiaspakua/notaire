package com.licensis.notaire.repository;

import com.licensis.notaire.negocio.Testimonio;
import com.licensis.notaire.negocio.Escritura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestimonioRepository extends JpaRepository<Testimonio, Integer> {

    List<Testimonio> findByFkIdEscritura(Escritura escritura);

    List<Testimonio> findByFkIdEscrituraIdEscritura(Integer idEscritura);

    @Query("SELECT t FROM Testimonio t WHERE t.estado = :estado")
    List<Testimonio> findByEstado(@Param("estado") String estado);

    @Query("SELECT t FROM Testimonio t WHERE t.numeroTestimonio = :numero")
    List<Testimonio> findByNumeroTestimonio(@Param("numero") String numero);
}
