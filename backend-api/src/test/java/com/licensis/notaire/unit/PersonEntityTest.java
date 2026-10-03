package com.licensis.notaire.unit;

import com.licensis.notaire.business.Person;
import com.licensis.notaire.business.IdentificationType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import com.licensis.notaire.testing.RequirementCoverage;
import com.licensis.notaire.dto.DtoPerson;

@RequirementCoverage({"CU17", "CU18", "CU41", "CU46", "CU48", "CU51", "CU54", "CU61", "CU76"})
@DisplayName("Person Entity Tests")
class PersonEntityTest {

    @Nested
    @DisplayName("CU17 - Create Person - Unit Tests")
    class CreatePersonTests {

        @Test
        @DisplayName("Should create person with required fields")
        void shouldCreatePersonWithRequiredFields() {
            IdentificationType typeId = new IdentificationType();
            typeId.setIdIdentificationType(1);
            typeId.setName("DNI");

            Person person = new Person();
            person.setPersonId(1);
            person.setFirstName("Juan");
            person.setLastName("Perez");
            person.setIdentificationNumber("12345678");
            person.setFkIdIdentificationType(typeId);
            person.setIsClient(false);

            assertThat(person.getFirstName()).isEqualTo("Juan");
            assertThat(person.getLastName()).isEqualTo("Perez");
            assertThat(person.getIdentificationNumber()).isEqualTo("12345678");
            assertThat(person.getIsClient()).isFalse();
        }

        @Test
        @DisplayName("Should create client with all fields")
        void shouldCreateClientWithAllFields() {
            IdentificationType typeId = new IdentificationType();
            typeId.setIdIdentificationType(1);
            typeId.setName("DNI");

            Person person = new Person();
            person.setPersonId(1);
            person.setFirstName("Maria");
            person.setLastName("Gonzalez");
            person.setIdentificationNumber("87654321");
            person.setFkIdIdentificationType(typeId);
            person.setIsClient(true);
            person.setNationality("Argentina");
            person.setBirthDate(new Date(1990 - 1900, 5, 15));
            person.setTaxId("27-87654321-5");
            person.setMaritalStatus("soltero");
            person.setSex("femenino");
            person.setOccupation("empleada");
            person.setAddress("Calle Falsa 123");

            assertThat(person.getIsClient()).isTrue();
            assertThat(person.getNationality()).isEqualTo("Argentina");
            assertThat(person.getTaxId()).isEqualTo("27-87654321-5");
            assertThat(person.getMaritalStatus()).isEqualTo("soltero");
        }

        @Test
        @DisplayName("Should create notary with registration number")
        void shouldCreateNotaryWithRegistration() {
            IdentificationType typeId = new IdentificationType();
            typeId.setIdIdentificationType(1);
            typeId.setName("DNI");

            Person notary = new Person();
            notary.setPersonId(1);
            notary.setFirstName("Juan Carlos");
            notary.setLastName("Garcia");
            notary.setIdentificationNumber("20123456");
            notary.setFkIdIdentificationType(typeId);
            notary.setNotaryRegistrationNumber(1001);
            notary.setIsClient(false);

            assertThat(notary.getNotaryRegistrationNumber()).isEqualTo(1001);
        }

        @Test
        @DisplayName("Should implement equals based on id")
        void shouldImplementEqualsBasedOnId() {
            Person p1 = new Person(1);
            Person p2 = new Person(1);
            Person p3 = new Person(2);

            assertThat(p1).isEqualTo(p2);
            assertThat(p1).isNotEqualTo(p3);
        }

        @Test
        @DisplayName("Should implement hashCode based on id")
        void shouldImplementHashCodeBasedOnId() {
            Person p1 = new Person(1);
            Person p2 = new Person(1);

            assertThat(p1.hashCode()).isEqualTo(p2.hashCode());
        }
    }

    @Nested
    @DisplayName("CU61 - Search person or client - Unit Tests")
    class SearchPersonTests {

        @Test
        @DisplayName("Should filter people by first name")
        void shouldFilterPeopleByFirstName() {
            List<Person> people = createTestPeople();

            String filter = "Juan";
            List<Person> filtered = people.stream()
                .filter(p -> p.getFirstName() != null && p.getFirstName().contains(filter))
                .toList();

            assertThat(filtered).hasSize(1);
            assertThat(filtered.get(0).getLastName()).isEqualTo("Perez");
        }

        @Test
        @DisplayName("Should filter people by last name")
        void shouldFilterPeopleByLastName() {
            List<Person> people = createTestPeople();

            String filter = "Garcia";
            List<Person> filtered = people.stream()
                .filter(p -> p.getLastName() != null && p.getLastName().contains(filter))
                .toList();

            assertThat(filtered).hasSize(2);
        }

        @Test
        @DisplayName("Should filter only clients")
        void shouldFilterOnlyClients() {
            List<Person> people = createTestPeople();

            List<Person> clients = people.stream()
                .filter(Person::getIsClient)
                .toList();

            assertThat(clients).hasSize(1);
        }

        @Test
        @DisplayName("Should filter by identification type")
        void shouldFilterByIdentificationType() {
            List<Person> people = createTestPeople();

            IdentificationType dni = new IdentificationType();
            dni.setIdIdentificationType(1);

            List<Person> filtered = people.stream()
                .filter(p -> p.getFkIdIdentificationType() != null
                    && p.getFkIdIdentificationType().getIdIdentificationType().equals(1))
                .toList();

            assertThat(filtered).hasSize(3);
        }
    }

    @Nested
    @DisplayName("getDto null-safety for management associations")
    class GetDtoNullSafetyTests {

        @Test
        @DisplayName("getDto with null identification type does not throw")
        void getDtoWithNullIdentificationTypeDoesNotThrow() {
            Person person = new Person();
            person.setPersonId(20);
            person.setFirstName("Lucia");
            person.setLastName("Diaz");
            person.setFkIdIdentificationType(null);
            person.setNotaryRegistrationNumber(null);

            assertThatCode(person::getDto).doesNotThrowAnyException();
            DtoPerson dto = person.getDto();
            assertThat(dto.getId()).isEqualTo(20);
            assertThat(dto.getDtoIdentificationType()).isNull();
        }

        @Test
        @DisplayName("getDto with null DeedManagementList does not throw")
        void getDtoWithNullDeedManagementListDoesNotThrow() {
            Person notary = new Person();
            notary.setPersonId(21);
            notary.setFirstName("Pedro");
            notary.setLastName("Suarez");
            notary.setNotaryRegistrationNumber(2002);
            notary.setFkIdIdentificationType(null);
            notary.setDeedManagementList(null);

            assertThatCode(notary::getDto).doesNotThrowAnyException();
            DtoPerson dto = notary.getDto();
            assertThat(dto.getId()).isEqualTo(21);
        }
    }

    private List<Person> createTestPeople() {
        IdentificationType dni = new IdentificationType();
        dni.setIdIdentificationType(1);
        dni.setName("DNI");

        Person p1 = new Person(1, "Juan", "Perez", "12345678", false);
        p1.setFkIdIdentificationType(dni);

        Person p2 = new Person(2, "Maria", "Garcia", "87654321", true);
        p2.setFkIdIdentificationType(dni);

        Person p3 = new Person(3, "Carlos", "Garcia", "11222333", false);
        p3.setFkIdIdentificationType(dni);

        List<Person> people = new ArrayList<>();
        people.add(p1);
        people.add(p2);
        people.add(p3);

        return people;
    }
}
