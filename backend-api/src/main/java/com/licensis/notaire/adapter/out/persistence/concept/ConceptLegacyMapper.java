package com.licensis.notaire.adapter.out.persistence.concept;

import com.licensis.notaire.business.Concept;
import com.licensis.notaire.dto.DtoConcept;
import com.licensis.notaire.dto.exceptions.InvalidDtoException;

/**
 * Maps the legacy {@link Concept} entity without leaving transport types on the entity (#580).
 */
public final class ConceptLegacyMapper {

    private ConceptLegacyMapper() {
    }

    public static DtoConcept toDto(Concept concept) {
        DtoConcept dto = new DtoConcept();
        dto.setIdConcept(concept.getIdConcept());
        dto.setName(concept.getName());
        dto.setValue(concept.getValue());
        dto.setPercentage(concept.getPercentage());
        dto.setVersion(concept.getVersion());
        dto.setEnabled(concept.getEnabled());
        dto.setFixed(concept.isFixedConcept());
        return dto;
    }

    public static void apply(Concept concept, DtoConcept dto) throws InvalidDtoException {
        concept.setIdConcept(dto.getIdConcept());
        if (dto.getName() != null) {
            concept.setName(dto.getName());
        }
        if (dto.getValue() != null) {
            concept.setValue(dto.getValue());
        }
        if (dto.getPercentage() != null) {
            concept.setPercentage(dto.getPercentage());
        }
        concept.setVersion(dto.getVersion());
        concept.setEnabled(dto.getEnabled());
        concept.setFixedConcept(dto.isFixed());
    }
}
