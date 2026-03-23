package com.licensis.notaire.repository;

import com.licensis.notaire.negocio.Historial;
import com.licensis.notaire.negocio.GestionDeEscritura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface HistorialRepository extends JpaRepository<Historial, Integer> {

    List<Historial> findByFkIdGestion(GestionDeEscritura gestion);

    List<Historial> findByFkIdGestionIdGestion(Integer idGestion);

    @Query("SELECT h FROM Historial h WHERE h.fecha BETWEEN :startDate AND :endDate")
    List<Historial> findByFechaCambioBetween(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    @Query("SELECT h FROM Historial h WHERE h.observaciones LIKE %:keyword%")
    List<Historial> findByDescripcionContaining(@Param("keyword") String keyword);
}
