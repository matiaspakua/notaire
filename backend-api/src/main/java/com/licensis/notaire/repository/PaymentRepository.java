package com.licensis.notaire.repository;

import com.licensis.notaire.business.Payment;
import com.licensis.notaire.business.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    List<Payment> findByFkIdBudget(Budget budget);

    List<Payment> findByFkIdBudgetIdBudget(Integer idBudget);

    List<Payment> findByAmount(Float amount);

    @Query("SELECT p FROM Payment p WHERE p.date BETWEEN :startDate AND :endDate")
    List<Payment> findByDateBetween(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.fkIdBudget.idBudget = :idPresupuesto")
    Float sumAmountByBudgetId(@Param("idPresupuesto") Integer idBudget);
}
