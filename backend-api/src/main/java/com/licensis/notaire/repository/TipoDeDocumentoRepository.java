package com.licensis.notaire.repository;

import com.licensis.notaire.negocio.TipoDeDocumento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TipoDeDocumentoRepository extends JpaRepository<TipoDeDocumento, Integer> {

    Optional<TipoDeDocumento> findByNombre(String nombre);

    boolean existsByNombre(String nombre);

    List<TipoDeDocumento> findByNombreContaining(String nombre);
}
