package com.licensis.notaire.adapter.out.persistence.user;

import com.licensis.notaire.adapter.out.persistence.person.PersonLegacyMapper;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.business.User;
import com.licensis.notaire.dto.DtoUser;

/** Transport mapping for {@link User}. The entity does not import {@link DtoUser}. */
public final class UserLegacyMapper {

    private UserLegacyMapper() {
    }

    public static void apply(User user, DtoUser dto) {
        Person person = new Person();
        PersonLegacyMapper.apply(person, dto.getPersons());
        user.setFkIdPerson(person);
        user.setPassword(dto.getPassword());
        user.setStatus(dto.isStatus());
        user.setIdUser(dto.getIdUser());
        user.setName(dto.getName());
        user.setType(dto.getType());
        if (dto.getVersion() != null) {
            user.setVersion(dto.getVersion());
        }
    }
}
