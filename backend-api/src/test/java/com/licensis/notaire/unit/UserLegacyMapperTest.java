package com.licensis.notaire.unit;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.licensis.notaire.adapter.out.persistence.user.UserLegacyMapper;
import com.licensis.notaire.business.User;
import com.licensis.notaire.dto.DtoIdentificationType;
import com.licensis.notaire.dto.DtoPerson;
import com.licensis.notaire.dto.DtoUser;

class UserLegacyMapperTest {

    @Test
    @DisplayName("apply copies the account and the linked person")
    void applyCopiesAccountAndPerson() {
        DtoIdentificationType type = new DtoIdentificationType();
        type.setIdIdentificationType(3);
        type.setName("DNI");
        DtoPerson person = new DtoPerson();
        person.setVersion(1);
        person.setFirstName("Ana");
        person.setId(9);
        person.setDtoIdentificationType(type);
        person.setIsClient(false);
        DtoUser dto = new DtoUser();
        dto.setIdUser(4);
        dto.setName("ana");
        dto.setPassword("hash");
        dto.setStatus(true);
        dto.setType("NOTARY");
        dto.setVersion(2);
        dto.setPersons(person);

        User user = new User();
        UserLegacyMapper.apply(user, dto);

        assertThat(user.getIdUser()).isEqualTo(4);
        assertThat(user.getName()).isEqualTo("ana");
        assertThat(user.getPassword()).isEqualTo("hash");
        assertThat(user.getStatus()).isTrue();
        assertThat(user.getType()).isEqualTo("NOTARY");
        assertThat(user.getVersion()).isEqualTo(2);
        assertThat(user.getFkIdPerson().getFirstName()).isEqualTo("Ana");
        assertThat(user.getFkIdPerson().getPersonId()).isEqualTo(9);
    }

    @Test
    @DisplayName("a null version leaves the current version")
    void nullVersionIsKept() {
        DtoIdentificationType type = new DtoIdentificationType();
        type.setIdIdentificationType(1);
        type.setName("DNI");
        DtoPerson person = new DtoPerson();
        person.setVersion(1);
        person.setDtoIdentificationType(type);
        DtoUser dto = new DtoUser();
        dto.setName("ana");
        dto.setPersons(person);

        User user = new User();
        user.setVersion(7);
        UserLegacyMapper.apply(user, dto);

        assertThat(user.getVersion()).isEqualTo(7);
    }

    @Test
    @DisplayName("the user entity no longer applies a transport DTO")
    void entityDoesNotMapItself() throws Exception {
        String source = Files.readString(Path.of("src/main/java/com/licensis/notaire/business/User.java"));
        assertThat(source).doesNotContain("setAttributes");
    }
}
