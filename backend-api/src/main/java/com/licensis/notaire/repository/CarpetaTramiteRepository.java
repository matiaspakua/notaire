package com.licensis.notaire.repository;

import com.licensis.notaire.negocio.CarpetaTramite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CarpetaTramiteRepository extends JpaRepository<CarpetaTramite, Integer> {

    Optional<CarpetaTramite> findByFkIdTramiteIdTramite(Integer idTramite);

    List<CarpetaTramite> findByFkIdGestionIdGestion(Integer idGestion);

    List<CarpetaTramite> findByFkIdGestionIdGestionAndEstado(Integer idGestion, String estado);

    Optional<CarpetaTramite> findTopByOrderByNumeroDesc();
}
