package com.licensis.notaire.application.port.out.person;

/** Existence check for people/notaries without exposing the JPA entity (issue #1727). */
public interface PersonLookupPort {

    boolean existsById(Integer id);
}
