package com.licensis.notaire.repository;

import com.licensis.notaire.negocio.Pago;
import com.licensis.notaire.negocio.Presupuesto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Integer> {

    List<Pago> findByFkIdPresupuesto(Presupuesto presupuesto);

    List<Pago> findByFkIdPresupuestoIdPresupuesto(Integer idPresupuesto);

    List<Pago> findByMonto(Float monto);

    @Query("SELECT p FROM Pago p WHERE p.fecha BETWEEN :startDate AND :endDate")
    List<Pago> findByFechaBetween(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    @Query("SELECT SUM(p.monto) FROM Pago p WHERE p.fkIdPresupuesto.idPresupuesto = :idPresupuesto")
    Float sumMontoByPresupuestoId(@Param("idPresupuesto") Integer idPresupuesto);
}
