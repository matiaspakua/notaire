package com.licensis.notaire.repository;

import com.licensis.notaire.negocio.Pago;
import com.licensis.notaire.negocio.Presupuesto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Integer> {

    List<Pago> findByFkIdPresupuesto(Presupuesto presupuesto);

    List<Pago> findByFkIdPresupuestoIdPresupuesto(Integer idPresupuesto);

    @Query("SELECT p FROM Pago p WHERE p.estado = :estado")
    List<Pago> findByEstado(@Param("estado") String estado);

    @Query("SELECT p FROM Pago p WHERE p.fechaPago BETWEEN :startDate AND :endDate")
    List<Pago> findByFechaPagoBetween(@Param("startDate") java.util.Date startDate, @Param("endDate") java.util.Date endDate);

    @Query("SELECT SUM(p.monto) FROM Pago p WHERE p.fkIdPresupuesto.idPresupuesto = :idPresupuesto AND p.estado = 'confirmado'")
    Float sumMontoByPresupuestoIdAndEstadoConfirmado(@Param("idPresupuesto") Integer idPresupuesto);
}
