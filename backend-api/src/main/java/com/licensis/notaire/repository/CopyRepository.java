package com.licensis.notaire.repository;

import com.licensis.notaire.business.Copy;
import com.licensis.notaire.business.Testimony;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CopyRepository extends JpaRepository<Copy, Integer> {

    List<Copy> findByFkIdTestimony(Testimony testimony);

    List<Copy> findByFkIdTestimonyIdTestimony(Integer idTestimony);

    List<Copy> findByFkIdPersonIdPerson(Integer idPerson);

    List<Copy> findByNumber(int number);
}
