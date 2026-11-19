package com.licensis.notaire.unit;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.licensis.notaire.adapter.out.persistence.person.PersonLegacyMapper;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.dto.DtoIdentificationType;
import com.licensis.notaire.dto.DtoPerson;

class PersonLegacyMapperTest {

    @Test
    @DisplayName("apply copies identity fields and client fields only when the person is a client")
    void applyCopiesClientFieldsOnlyForClients() {
        DtoIdentificationType type = new DtoIdentificationType();
        type.setIdIdentificationType(3);
        type.setName("DNI");
        DtoPerson dto = new DtoPerson();
        dto.setVersion(2);
        dto.setFirstName("Ana");
        dto.setLastName("Paz");
        dto.setPhone("1");
        dto.setEmail("a@b.c");
        dto.setId(9);
        dto.setIdentificationNumber("30111222");
        dto.setDtoIdentificationType(type);
        dto.setIsClient(true);
        dto.setNationality("AR");
        dto.setOccupation("notary");

        Person person = new Person();
        PersonLegacyMapper.apply(person, dto);

        assertThat(person.getVersion()).isEqualTo(2);
        assertThat(person.getFirstName()).isEqualTo("Ana");
        assertThat(person.getLastName()).isEqualTo("Paz");
        assertThat(person.getPersonId()).isEqualTo(9);
        assertThat(person.getFkIdIdentificationType().getIdIdentificationType()).isEqualTo(3);
        assertThat(person.getFkIdIdentificationType().getName()).isEqualTo("DNI");
        assertThat(person.getNationality()).isEqualTo("AR");
        assertThat(person.getOccupation()).isEqualTo("notary");

        DtoPerson staff = new DtoPerson();
        staff.setVersion(1);
        staff.setFirstName("Luis");
        staff.setDtoIdentificationType(type);
        staff.setIsClient(false);
        staff.setNationality("UY");
        Person other = new Person();
        PersonLegacyMapper.apply(other, staff);
        assertThat(other.getFirstName()).isEqualTo("Luis");
        assertThat(other.getNationality()).isNull();
    }

    @Test
    @DisplayName("the person entity no longer applies a transport DTO")
    void entityDoesNotMapItself() throws Exception {
        String source = Files.readString(Path.of("src/main/java/com/licensis/notaire/business/Person.java"));
        assertThat(source).doesNotContain("setAttributes");
    }
}
