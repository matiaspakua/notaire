package com.licensis.notaire.repository;

import com.licensis.notaire.business.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Integer> {

    @Query("SELECT i FROM Property i WHERE i.address LIKE %:domicilio%")
    List<Property> findByAddressContaining(@Param("domicilio") String address);

    @Query("SELECT i FROM Property i WHERE i.cadastralDesignation LIKE %:nomenclatura%")
    List<Property> findByDesignationContaining(@Param("nomenclatura") String designation);

    List<Property> findByCadastralDesignation(String cadastralDesignation);
}
