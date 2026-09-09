package com.licensis.notaire.repository;

import com.licensis.notaire.business.History;
import com.licensis.notaire.business.DeedManagement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface HistoryRepository extends JpaRepository<History, Integer> {

    List<History> findByFkIdManagement(DeedManagement management);

    List<History> findByFkIdManagementIdManagement(Integer idManagement);

    @Query("SELECT h FROM History h WHERE h.date BETWEEN :startDate AND :endDate")
    List<History> findByDateCambioBetween(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    @Query("SELECT h FROM History h WHERE h.notes LIKE %:keyword%")
    List<History> findByDescriptionContaining(@Param("keyword") String keyword);
}
