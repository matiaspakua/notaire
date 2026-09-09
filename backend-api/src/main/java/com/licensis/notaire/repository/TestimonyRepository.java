package com.licensis.notaire.repository;

import com.licensis.notaire.business.Testimony;
import com.licensis.notaire.business.Deed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestimonyRepository extends JpaRepository<Testimony, Integer> {

    List<Testimony> findByFkIdDeed(Deed deed);

    List<Testimony> findByFkIdDeedIdDeed(Integer idDeed);

    List<Testimony> findByFlagged(boolean flagged);

    @Query("SELECT t FROM Testimony t WHERE t.number = :numero")
    List<Testimony> findByNumberTestimony(@Param("numero") int number);
}
