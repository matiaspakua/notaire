package com.licensis.notaire.repository;

import com.licensis.notaire.negocio.Cuaderno;
import com.licensis.notaire.negocio.Folio;
import com.licensis.notaire.negocio.Persona;
import com.licensis.notaire.negocio.TipoDeFolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FolioRepository extends JpaRepository<Folio, Integer> {

    Optional<Folio> findByNumero(int numero);

    List<Folio> findByFkIdTipoFolio(TipoDeFolio tipoFolio);

    List<Folio> findByFkIdTipoFolioIdTipoFolio(Integer idTipoFolio);

    List<Folio> findByFkIdPersonaEscribano(Persona escribano);

    List<Folio> findByFkIdPersonaEscribanoIdPersona(Integer idEscribano);

    boolean existsByFkIdEscrituraIdEscritura(Integer idEscritura);

    List<Folio> findByAnio(int anio);

    List<Folio> findByEstado(String estado);

    List<Folio> findByFkIdCuaderno(Cuaderno cuaderno);

    List<Folio> findAllByIdFolioIn(List<Integer> ids);
}
