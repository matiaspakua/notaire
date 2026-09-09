package com.licensis.notaire.unit;

import com.licensis.notaire.dto.DtoDeed;
import com.licensis.notaire.dto.DtoDeedManagement;
import com.licensis.notaire.dto.DtoProperty;
import com.licensis.notaire.dto.DtoBudget;
import com.licensis.notaire.dto.DtoProcedureType;
import com.licensis.notaire.dto.DtoProcedure;
import com.licensis.notaire.business.Deed;
import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.Property;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.business.Budget;
import com.licensis.notaire.business.ProcedureType;
import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.testing.RequirementCoverage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RequirementCoverage({"CU02", "CU13", "CU14"})
@DisplayName("Tramite Entity Tests")
class ProcedureEntityTest {

    @Nested
    @DisplayName("Constructor and default state")
    class ConstructorTests {

        @Test
        @DisplayName("Should initialize with default constructor")
        void shouldInitializeWithDefaultConstructor() {
            Procedure procedure = new Procedure();

            assertThat(procedure.getIdProcedure()).isNotNull();
            assertThat(procedure.getSubmittedDocumentList()).isNotNull().isEmpty();
            assertThat(procedure.getPersonList()).isNotNull().isEmpty();
        }

        @Test
        @DisplayName("Should initialize with ID constructor")
        void shouldInitializeWithIdConstructor() {
            Procedure procedure = new Procedure(42);

            assertThat(procedure.getIdProcedure()).isEqualTo(42);
        }
    }

    @Nested
    @DisplayName("Field getters and setters")
    class FieldTests {

        @Test
        @DisplayName("Should set and get notes")
        void shouldSetAndGetNotes() {
            Procedure procedure = new Procedure();
            procedure.setNotes("Compraventa de property urbano");

            assertThat(procedure.getNotes()).isEqualTo("Compraventa de property urbano");
        }

        @Test
        @DisplayName("Should set and get version")
        void shouldSetAndGetVersion() {
            Procedure procedure = new Procedure();
            procedure.setVersion(3);

            assertThat(procedure.getVersion()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should set and get type de tramite")
        void shouldSetAndGetProcedureType() {
            ProcedureType type = new ProcedureType();
            type.setIdProcedureType(10);
            type.setName("Compraventa");

            Procedure procedure = new Procedure();
            procedure.setFkIdProcedureType(type);

            assertThat(procedure.getFkIdProcedureType()).isNotNull();
            assertThat(procedure.getFkIdProcedureType().getName()).isEqualTo("Compraventa");
        }

        @Test
        @DisplayName("Should set and get property")
        void shouldSetAndGetProperty() {
            Property property = new Property();
            property.setCadastralDesignation("15-02-03-04-0005");

            Procedure procedure = new Procedure();
            procedure.setFkIdProperty(property);

            assertThat(procedure.getFkIdProperty()).isNotNull();
            assertThat(procedure.getFkIdProperty().getCadastralDesignation()).isEqualTo("15-02-03-04-0005");
        }

        @Test
        @DisplayName("Should set and get budget")
        void shouldSetAndGetBudget() {
            Budget budget = new Budget();
            budget.setIdBudget(100);

            Procedure procedure = new Procedure();
            procedure.setFkIdBudget(budget);

            assertThat(procedure.getFkIdBudget()).isNotNull();
            assertThat(procedure.getFkIdBudget().getIdBudget()).isEqualTo(100);
        }

        @Test
        @DisplayName("Should set and get deed")
        void shouldSetAndGetDeed() {
            Deed deed = new Deed();
            deed.setNumber(2025001);

            Procedure procedure = new Procedure();
            procedure.setFkIdDeed(deed);

            assertThat(procedure.getFkIdDeed()).isNotNull();
            assertThat(procedure.getFkIdDeed().getNumber()).isEqualTo(2025001);
        }

        @Test
        @DisplayName("Should set and get gestion de deed")
        void shouldSetAndGetDeedManagement() {
            DeedManagement management = new DeedManagement();
            management.setNumber(5001);

            Procedure procedure = new Procedure();
            procedure.setFkIdManagement(management);

            assertThat(procedure.getFkIdManagement()).isNotNull();
            assertThat(procedure.getFkIdManagement().getNumber()).isEqualTo(5001);
        }

        @Test
        @DisplayName("Should set and get person list")
        void shouldSetAndGetPersonList() {
            Person person = new Person();
            person.setFirstName("Juan");
            List<Person> persons = new ArrayList<>();
            persons.add(person);

            Procedure procedure = new Procedure();
            procedure.setPersonList(persons);

            assertThat(procedure.getPersonList()).hasSize(1);
            assertThat(procedure.getPersonList().get(0).getFirstName()).isEqualTo("Juan");
        }
    }

    @Nested
    @DisplayName("Equality and identity")
    class EqualityTests {

        @Test
        @DisplayName("Should be equal when same ID")
        void shouldBeEqualWhenSameId() {
            Procedure t1 = new Procedure(1);
            Procedure t2 = new Procedure(1);

            assertThat(t1).isEqualTo(t2);
            assertThat(t1.hashCode()).isEqualTo(t2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when different IDs")
        void shouldNotBeEqualWhenDifferentIds() {
            Procedure t1 = new Procedure(1);
            Procedure t2 = new Procedure(2);

            assertThat(t1).isNotEqualTo(t2);
        }

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            Procedure procedure = new Procedure(1);

            assertThat(procedure).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Should not be equal to different type")
        void shouldNotBeEqualToDifferentType() {
            Procedure procedure = new Procedure(1);

            assertThat(procedure).isNotEqualTo("not a tramite");
        }

        @Test
        @DisplayName("toString should include ID")
        void toStringShouldIncludeId() {
            Procedure procedure = new Procedure(99);

            assertThat(procedure.toString()).contains("99");
        }

        @Test
        @DisplayName("hashCode should be zero when ID is null")
        void hashCodeShouldBeZeroWhenIdIsNull() {
            Procedure procedure = new Procedure();
            procedure.setIdProcedure(null);

            assertThat(procedure.hashCode()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("setAtributos branches")
    class SetAtributosTests {

        private DtoProcedure baseDto() {
            DtoProcedureType typeDto = new DtoProcedureType();
            typeDto.setIdProcedureType(1);
            typeDto.setName("Compraventa");
            DtoProcedure dto = new DtoProcedure();
            dto.setIdProcedure(10);
            dto.setNotes("obs");
            dto.setTiposDeProcedure(typeDto);
            return dto;
        }

        @Test
        @DisplayName("setAtributos with all optional fields null — covers null branches")
        void setAtributosAllNullOptional() {
            DtoProcedure dto = baseDto();
            dto.setProperty(null);

            Procedure procedure = new Procedure();
            procedure.setAtributos(dto);

            assertThat(procedure.getIdProcedure()).isEqualTo(10);
            assertThat(procedure.getFkIdProperty()).isNull();
            assertThat(procedure.getFkIdDeed()).isNull();
            assertThat(procedure.getFkIdManagement()).isNull();
            assertThat(procedure.getFkIdBudget()).isNull();
        }

        @Test
        @DisplayName("setAtributos with non-null property — covers non-null branch")
        void setAtributosWithProperty() {
            DtoProcedure dto = baseDto();
            DtoProperty dtoProperty = new DtoProperty();
            dtoProperty.setIdProperty(5);
            dtoProperty.setAddress("Calle Test 123");
            dto.setProperty(dtoProperty);

            Procedure procedure = new Procedure();
            procedure.setAtributos(dto);

            assertThat(procedure.getFkIdProperty()).isNotNull();
        }

        @Test
        @DisplayName("setAtributos with non-null deed — covers non-null branch")
        void setAtributosWithDeed() {
            DtoProcedure dto = baseDto();
            DtoDeed dtoDeed = new DtoDeed();
            dtoDeed.setIdDeed(7);
            dtoDeed.setNumber(2025001);
            dto.setDeed(dtoDeed);

            Procedure procedure = new Procedure();
            procedure.setAtributos(dto);

            assertThat(procedure.getFkIdDeed()).isNotNull();
        }

        @Test
        @DisplayName("setAtributos with non-null gestion — covers non-null branch")
        void setAtributosWithManagement() {
            DtoProcedure dto = baseDto();
            DtoDeedManagement dtoManagement = new DtoDeedManagement();
            dtoManagement.setIdManagement(3);
            dto.setDeedManagement(dtoManagement);

            Procedure procedure = new Procedure();
            procedure.setAtributos(dto);

            assertThat(procedure.getFkIdManagement()).isNotNull();
        }

        @Test
        @DisplayName("setAtributos with non-null budget — covers non-null branch")
        void setAtributosWithBudget() {
            DtoProcedure dto = baseDto();
            DtoBudget dtoBudget = new DtoBudget();
            dtoBudget.setIdBudget(20);
            dtoBudget.setNumber(12345);
            dtoBudget.setVersion(0);
            dto.setBudget(dtoBudget);

            Procedure procedure = new Procedure();
            procedure.setAtributos(dto);

            assertThat(procedure.getFkIdBudget()).isNotNull();
        }
    }

    @Nested
    @DisplayName("getDto branches")
    class GetDtoTests {

        private Procedure baseProcedure() {
            Procedure procedure = new Procedure(5);
            ProcedureType type = new ProcedureType(1);
            type.setName("Compraventa");
            procedure.setFkIdProcedureType(type);
            return procedure;
        }

        @Test
        @DisplayName("getDto with all optional refs null — covers null branches")
        void getDtoAllNullOptional() {
            Procedure procedure = baseProcedure();

            var dto = procedure.getDto();

            assertThat(dto.getIdProcedure()).isEqualTo(5);
            assertThat(dto.getDeed()).isNull();
            assertThat(dto.getManagement()).isNull();
            assertThat(dto.getProperty()).isNull();
            assertThat(dto.getBudget()).isNull();
        }

        @Test
        @DisplayName("getDto with deed set — covers non-null deed branch")
        void getDtoWithDeed() {
            Procedure procedure = baseProcedure();
            Deed deed = new Deed(7);
            deed.setNumber(2025001);
            procedure.setFkIdDeed(deed);

            var dto = procedure.getDto();

            assertThat(dto.getDeed()).isNotNull();
            assertThat(dto.getDeed().getIdDeed()).isEqualTo(7);
        }

        @Test
        @DisplayName("getDto with property set — covers non-null property branch")
        void getDtoWithProperty() {
            Procedure procedure = baseProcedure();
            Property property = new Property(3);
            property.setAddress("Calle Test 456");
            procedure.setFkIdProperty(property);

            var dto = procedure.getDto();

            assertThat(dto.getProperty()).isNotNull();
        }

        @Test
        @DisplayName("getDto with budget set — covers non-null budget branch")
        void getDtoWithBudget() {
            Procedure procedure = baseProcedure();
            Budget budget = new Budget(15);
            procedure.setFkIdBudget(budget);

            var dto = procedure.getDto();

            assertThat(dto.getBudget()).isNotNull();
            assertThat(dto.getBudget().getIdBudget()).isEqualTo(15);
        }

        @Test
        @DisplayName("getDto with gestion set — covers non-null gestion branch")
        void getDtoWithManagement() {
            Procedure procedure = baseProcedure();
            DeedManagement management = new DeedManagement();
            management.setIdManagement(9);
            management.setNumber(5001);
            Person notary = new Person(3);
            notary.setNotaryRegistrationNumber(1001);
            management.setFkIdNotaryPerson(notary);
            procedure.setFkIdManagement(management);

            var dto = procedure.getDto();

            assertThat(dto.getManagement()).isNotNull();
            assertThat(dto.getManagement().getIdManagement()).isEqualTo(9);
        }
    }
}
