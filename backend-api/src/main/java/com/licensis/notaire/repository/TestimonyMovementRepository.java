package com.licensis.notaire.repository;

import com.licensis.notaire.business.TestimonyMovement;
import com.licensis.notaire.business.Testimony;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface TestimonyMovementRepository extends JpaRepository<TestimonyMovement, Integer> {

    List<TestimonyMovement> findByFkIdTestimony(Testimony testimony);

    List<TestimonyMovement> findByFkIdTestimonyIdTestimony(Integer idTestimony);

    Optional<TestimonyMovement> findTopByFkIdTestimonyIdTestimonyOrderByIdTestimonyMovementDesc(Integer idTestimony);

    List<TestimonyMovement> findByRegistered(boolean registered);

    boolean existsByFkIdTestimonyIdTestimonyAndRegisteredTrue(Integer idTestimony);

    @Query("SELECT m FROM TestimonyMovement m WHERE m.dateEntry BETWEEN :startDate AND :endDate")
    List<TestimonyMovement> findByDateMovementBetween(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
}
