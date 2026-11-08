package com.licensis.notaire.application.port.out.procedure;

/** Existence check for procedures without exposing the JPA entity (issue #1726). */
public interface ProcedureLookupPort {

    boolean existsById(Integer id);
}
