package com.licensis.notaire.repository;

import com.licensis.notaire.business.Deed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeedRepository extends JpaRepository<Deed, Integer> {

    Optional<Deed> findByNumber(Integer number);

    List<Deed> findByStatus(String status);
}
