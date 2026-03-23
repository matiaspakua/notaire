package com.licensis.notaire.repository;

import com.licensis.notaire.negocio.Item;
import com.licensis.notaire.negocio.Presupuesto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Integer> {

    List<Item> findByFkIdPresupuesto(Presupuesto presupuesto);

    List<Item> findByFkIdPresupuestoIdPresupuesto(Integer idPresupuesto);

    @Query("SELECT i FROM Item i WHERE i.nombre LIKE %:nombre%")
    List<Item> findByNombreItemContaining(@Param("nombre") String nombre);
}
