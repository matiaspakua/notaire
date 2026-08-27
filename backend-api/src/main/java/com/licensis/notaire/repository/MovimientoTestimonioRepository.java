package com.licensis.notaire.repository;

import com.licensis.notaire.negocio.MovimientoTestimonio;
import com.licensis.notaire.negocio.Testimonio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface MovimientoTestimonioRepository extends JpaRepository<MovimientoTestimonio, Integer> {

    List<MovimientoTestimonio> findByFkIdTestimonio(Testimonio testimonio);

    List<MovimientoTestimonio> findByFkIdTestimonioIdTestimonio(Integer idTestimonio);

    Optional<MovimientoTestimonio> findTopByFkIdTestimonioIdTestimonioOrderByIdMovimientoTestimonioDesc(Integer idTestimonio);

    List<MovimientoTestimonio> findByInscripta(boolean inscripta);

    @Query("SELECT m FROM MovimientoTestimonio m WHERE m.fechaIngreso BETWEEN :startDate AND :endDate")
    List<MovimientoTestimonio> findByFechaMovimientoBetween(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
}
