package com.licensis.notaire.repository;

import com.licensis.notaire.negocio.MovimientoTestimonio;
import com.licensis.notaire.negocio.Testimonio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovimientoTestimonioRepository extends JpaRepository<MovimientoTestimonio, Integer> {

    List<MovimientoTestimonio> findByFkIdTestimonio(Testimonio testimonio);

    List<MovimientoTestimonio> findByFkIdTestimonioIdTestimonio(Integer idTestimonio);

    @Query("SELECT m FROM MovimientoTestimonio m WHERE m.estado = :estado")
    List<MovimientoTestimonio> findByEstado(@Param("estado") String estado);

    @Query("SELECT m FROM MovimientoTestimonio m WHERE m.fechaMovimiento BETWEEN :startDate AND :endDate")
    List<MovimientoTestimonio> findByFechaMovimientoBetween(@Param("startDate") java.util.Date startDate, @Param("endDate") java.util.Date endDate);
}
