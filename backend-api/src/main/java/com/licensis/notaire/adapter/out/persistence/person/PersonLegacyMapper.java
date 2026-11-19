package com.licensis.notaire.adapter.out.persistence.person;

import com.licensis.notaire.business.IdentificationType;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.dto.DtoPerson;

/** Transport mapping for {@link Person}. The entity does not import {@link DtoPerson}. */
public final class PersonLegacyMapper {

    private PersonLegacyMapper() {
    }

    public static void apply(Person person, DtoPerson dto) {
        person.setVersion(dto.getVersion());
        person.setFirstName(dto.getFirstName());
        person.setLastName(dto.getLastName());
        person.setPhone(dto.getPhone());
        person.setEmail(dto.getEmail());
        person.setPersonId(dto.getId());
        person.setIdentificationNumber(dto.getIdentificationNumber());

        IdentificationType identificationType = new IdentificationType();
        identificationType.setIdIdentificationType(dto.getDtoIdentificationType().getIdIdentificationType());
        identificationType.setName(dto.getDtoIdentificationType().getName());
        person.setFkIdIdentificationType(identificationType);

        if (dto.getIsClient()) {
            person.setNationality(dto.getNationality());
            person.setBirthDate(dto.getBirthDate());
            person.setTaxId(dto.getTaxId());
            person.setMaritalStatus(dto.getMaritalStatus());
            person.setMarriageCount(dto.getMarriageCount());
            person.setSex(dto.getSex());
            person.setOccupation(dto.getOccupation());
            person.setAddress(dto.getAddress());
            person.setIsClient(dto.getIsClient());
        }
    }
}
