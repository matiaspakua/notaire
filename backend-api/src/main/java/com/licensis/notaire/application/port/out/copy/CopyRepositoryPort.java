package com.licensis.notaire.application.port.out.copy;

import com.licensis.notaire.business.Copy;
import com.licensis.notaire.business.Testimony;

import java.util.List;
import java.util.Optional;

/**
 * Outbound port for copy persistence operations.
 */
public interface CopyRepositoryPort {

    List<Copy> findAll();

    Optional<Copy> findById(Integer id);

    Copy save(Copy entity);

    void deleteById(Integer id);

    boolean existsById(Integer id);

    List<Copy> findByFkIdTestimony(Testimony testimony);

    List<Copy> findByFkIdTestimonyIdTestimony(Integer idTestimony);

    List<Copy> findByFkIdPersonIdPerson(Integer idPerson);

    List<Copy> findByNumber(int number);
}
