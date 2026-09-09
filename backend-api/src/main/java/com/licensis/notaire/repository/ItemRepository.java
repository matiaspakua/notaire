package com.licensis.notaire.repository;

import com.licensis.notaire.business.Item;
import com.licensis.notaire.business.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Integer> {

    List<Item> findByFkIdBudget(Budget budget);

    List<Item> findByFkIdBudgetIdBudget(Integer idBudget);

    @Query("SELECT i FROM Item i WHERE i.name LIKE %:nombre%")
    List<Item> findByNameItemContaining(@Param("nombre") String name);
}
