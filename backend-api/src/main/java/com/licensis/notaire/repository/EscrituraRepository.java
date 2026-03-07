package com.licensis.notaire.repository;

import com.licensis.notaire.negocio.Escritura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EscrituraRepository extends JpaRepository<Escritura, Integer> {

    Optional<Escritura> findByNumero(Integer numero);

    List<Escritura> findByEstado(String estado);
}
