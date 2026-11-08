package com.licensis.notaire.application.port.out.deed;

/** Existence check for deeds without exposing the JPA entity (issue #1727). */
public interface DeedLookupPort {

    boolean existsById(Integer id);
}
