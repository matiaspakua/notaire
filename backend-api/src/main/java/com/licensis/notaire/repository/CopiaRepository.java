package com.licensis.notaire.repository;

import com.licensis.notaire.negocio.Copia;
import com.licensis.notaire.negocio.Escritura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CopiaRepository extends JpaRepository<Copia, Integer> {

    List<Copia> findByFkIdEscritura(Escritura escritura);

    List<Copia> findByFkIdEscrituraIdEscritura(Integer idEscritura);

    @Query("SELECT c FROM Copia c WHERE c.estado = :estado")
    List<Copia> findByEstado(@Param("estado") String estado);

    @Query("SELECT c FROM Copia c WHERE c.tipoCopia = :tipoCopia")
    List<Copia> findByTipoCopia(@Param("tipoCopia") String tipoCopia);
}
