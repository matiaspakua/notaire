package com.licensis.notaire.repository;

import com.licensis.notaire.business.FolioType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FolioTypeRepository extends JpaRepository<FolioType, Integer> {

    Optional<FolioType> findByName(String name);

    boolean existsByName(String name);

    @Query("SELECT t FROM FolioType t WHERE t.name LIKE %:nombre%")
    List<FolioType> findByNameContaining(@Param("nombre") String name);
}
