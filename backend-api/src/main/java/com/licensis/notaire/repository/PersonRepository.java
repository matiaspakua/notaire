package com.licensis.notaire.repository;

import com.licensis.notaire.business.Person;
import com.licensis.notaire.business.IdentificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PersonRepository extends JpaRepository<Person, Integer> {

    Optional<Person> findByIdentificationNumber(String identificationNumber);

    List<Person> findByIsClient(boolean isClient);

    List<Person> findBySex(String sex);

    @Query("SELECT p FROM Person p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Person> findByNameContainingIgnoreCase(@Param("name") String name);

    @Query("SELECT p FROM Person p WHERE LOWER(p.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))")
    List<Person> findByLastNameContainingIgnoreCase(@Param("lastName") String lastName);

    @Query("SELECT p FROM Person p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')) "
            + "AND LOWER(p.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))")
    List<Person> findByNameAndLastNameContainingIgnoreCase(@Param("name") String name,
            @Param("lastName") String lastName);

    List<Person> findByFkIdIdentificationType(IdentificationType identificationType);

    List<Person> findByFkIdIdentificationTypeIdIdentificationType(Integer idIdentificationType);

    @Query("SELECT p FROM Person p WHERE p.notaryRegistrationNumber IS NOT NULL")
    List<Person> findAllNotaries();

    @Query("SELECT p FROM Person p WHERE p.isClient = true")
    List<Person> findAllClients();
}
