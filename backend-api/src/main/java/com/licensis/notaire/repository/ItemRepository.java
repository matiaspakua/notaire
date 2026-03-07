package com.licensis.notaire.repository;

import com.licensis.notaire.negocio.Item;
import com.licensis.notaire.negocio.Concepto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Integer> {

    List<Item> findByFkIdConcepto(Concepto concepto);

    List<Item> findByFkIdConceptoIdConcepto(Integer idConcepto);

    @Query("SELECT i FROM Item i WHERE i.nombreItem LIKE %:nombre%")
    List<Item> findByNombreItemContaining(@Param("nombre") String nombre);

    @Query("SELECT i FROM Item i WHERE i.estado = :estado")
    List<Item> findByEstado(@Param("estado") String estado);
}
