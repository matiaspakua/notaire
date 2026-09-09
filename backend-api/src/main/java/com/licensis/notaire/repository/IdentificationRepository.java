package com.licensis.notaire.repository;

import com.licensis.notaire.business.Identification;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.business.IdentificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IdentificationRepository extends JpaRepository<Identification, Integer> {

    List<Identification> findByPerson(Person person);

    List<Identification> findByPersonIdPerson(Integer idPerson);

    List<Identification> findByIdentificationType(IdentificationType identificationType);

    List<Identification> findByIdentificationTypeIdIdentificationType(Integer idIdentificationType);

    List<Identification> findByNumber(int number);
}
