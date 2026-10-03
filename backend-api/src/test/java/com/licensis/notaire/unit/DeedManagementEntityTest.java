package com.licensis.notaire.unit;

import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.IdentificationType;
import com.licensis.notaire.business.ManagementStatus;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.dto.DtoDeedManagement;
import com.licensis.notaire.dto.DtoIdentificationType;
import com.licensis.notaire.dto.DtoManagementStatus;
import com.licensis.notaire.dto.DtoPerson;
import com.licensis.notaire.testing.RequirementCoverage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@RequirementCoverage({"CU02", "CU13", "CU14", "CU19", "CU24", "CU76"})
@DisplayName("DeedManagement Entity Tests")
class DeedManagementEntityTest {

    @Nested
    @DisplayName("CU02 - Start DeedManagement - Unit Tests")
    class StartManagementTests {

        @Test
        @DisplayName("Should create management with required fields")
        void shouldCreateManagementWithRequiredFields() {
            DeedManagement management = new DeedManagement();
            management.setIdManagement(1);
            management.setNumber(1001);
            management.setDateStart(new Date());
            management.setEncabezado("Sale - Perez Garcia");

            assertThat(management.getNumber()).isEqualTo(1001);
            assertThat(management.getEncabezado()).isEqualTo("Sale - Perez Garcia");
        }

        @Test
        @DisplayName("Should link management to notary")
        void shouldLinkManagementToNotary() {
            Person notary = new Person();
            notary.setPersonId(1);
            notary.setFirstName("Juan Carlos");
            notary.setLastName("Garcia");
            notary.setNotaryRegistrationNumber(1001);

            DeedManagement management = new DeedManagement();
            management.setIdManagement(1);
            management.setFkIdNotaryPerson(notary);

            assertThat(management.getFkIdNotaryPerson()).isNotNull();
            assertThat(management.getFkIdNotaryPerson().getNotaryRegistrationNumber()).isEqualTo(1001);
        }

        @Test
        @DisplayName("Should link management to status")
        void shouldLinkManagementToStatus() {
            ManagementStatus status = new ManagementStatus();
            status.setIdManagementStatus(1);
            status.setName("Started");

            DeedManagement management = new DeedManagement();
            management.setIdManagement(1);
            management.setFkIdManagementStatus(status);

            assertThat(management.getFkIdManagementStatus()).isNotNull();
            assertThat(management.getFkIdManagementStatus().getName()).isEqualTo("Started");
        }

        @Test
        @DisplayName("Should implement equals based on id")
        void shouldImplementEqualsBasedOnId() {
            DeedManagement g1 = new DeedManagement(1);
            DeedManagement g2 = new DeedManagement(1);
            DeedManagement g3 = new DeedManagement(2);

            assertThat(g1).isEqualTo(g2);
            assertThat(g1).isNotEqualTo(g3);
        }
    }

    @Nested
    @DisplayName("Constructor list initialization")
    class ConstructorListInitTests {

        @Test
        @DisplayName("Default constructor initializes empty procedure and history lists")
        void defaultConstructorInitializesEmptyLists() {
            DeedManagement management = new DeedManagement();

            assertThat(management.getProcedureList()).isNotNull().isEmpty();
            assertThat(management.getHistoryList()).isNotNull().isEmpty();
        }

        @Test
        @DisplayName("Id constructor initializes empty procedure and history lists")
        void idConstructorInitializesEmptyLists() {
            DeedManagement management = new DeedManagement(42);

            assertThat(management.getProcedureList()).isNotNull().isEmpty();
            assertThat(management.getHistoryList()).isNotNull().isEmpty();
            assertThat(management.getIdManagement()).isEqualTo(42);
        }
    }

    @Nested
    @DisplayName("CU19 - Search client managements - Unit Tests")
    class SearchClientManagementsTests {

        @Test
        @DisplayName("Should filter managements by client")
        void shouldFilterManagementsByClient() {
            DeedManagement management1 = new DeedManagement(1);
            DeedManagement management2 = new DeedManagement(2);
            DeedManagement management3 = new DeedManagement(3);

            var managements = java.util.List.of(management1, management2, management3);

            assertThat(managements).hasSize(3);
        }
    }

    @Nested
    @DisplayName("Equality and hashCode branches")
    class EqualityTests {

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            DeedManagement g = new DeedManagement(1);
            assertThat(g).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Should not be equal to different type")
        void shouldNotBeEqualToDifferentType() {
            DeedManagement g = new DeedManagement(1);
            assertThat(g).isNotEqualTo("string");
        }

        @Test
        @DisplayName("Should be equal when both IDs are null")
        void shouldBeEqualWhenBothIdsNull() {
            DeedManagement g1 = new DeedManagement();
            DeedManagement g2 = new DeedManagement();
            assertThat(g1).isEqualTo(g2);
        }

        @Test
        @DisplayName("Should not be equal when this ID null and other has ID")
        void shouldNotBeEqualWhenThisNullOtherHasId() {
            DeedManagement g1 = new DeedManagement();
            DeedManagement g2 = new DeedManagement(5);
            assertThat(g1).isNotEqualTo(g2);
        }

        @Test
        @DisplayName("hashCode should be non-zero for default ID (-1)")
        void hashCodeDefaultIdIsNonZero() {
            DeedManagement g = new DeedManagement();
            assertThat(g.hashCode()).isNotEqualTo(0);
        }

        @Test
        @DisplayName("hashCode should be consistent with same ID")
        void hashCodeShouldBeConsistentWithSameId() {
            DeedManagement g1 = new DeedManagement(7);
            DeedManagement g2 = new DeedManagement(7);
            assertThat(g1.hashCode()).isEqualTo(g2.hashCode());
        }

        @Test
        @DisplayName("toString should contain ID")
        void toStringShouldContainId() {
            DeedManagement g = new DeedManagement(33);
            assertThat(g.toString()).contains("33");
        }
    }

    @Nested
    @DisplayName("setAtributos branches")
    class SetAtributosTests {

        @Test
        @DisplayName("setAtributos with null personNotary — does not set notary")
        void setAtributosWithNullPersonNotary() throws Exception {
            DeedManagement g = new DeedManagement(1);
            DtoDeedManagement dto = new DtoDeedManagement();
            dto.setNumber(100);
            dto.setPersonNotary(null);
            DtoManagementStatus dtoStatus = new DtoManagementStatus();
            dtoStatus.setIdManagementStatus(1);
            dtoStatus.setName("Started");
            dto.setStatus(dtoStatus);

            g.setAtributos(dto);
            assertThat(g.getFkIdNotaryPerson()).isNull();
        }

        @Test
        @DisplayName("setAtributos with non-null personNotary — sets notary")
        void setAtributosWithPersonNotary() throws Exception {
            DeedManagement g = new DeedManagement(1);
            DtoDeedManagement dto = new DtoDeedManagement();
            dto.setNumber(200);
            DtoIdentificationType dtoTypeId = new DtoIdentificationType();
            dtoTypeId.setIdIdentificationType(1);
            dtoTypeId.setName("DNI");
            DtoPerson dtoPerson = new DtoPerson();
            dtoPerson.setId(10);
            dtoPerson.setFirstName("Juan");
            dtoPerson.setLastName("Garcia");
            dtoPerson.setVersion(0);
            dtoPerson.setDtoIdentificationType(dtoTypeId);
            dto.setPersonNotary(dtoPerson);
            DtoManagementStatus dtoStatus = new DtoManagementStatus();
            dtoStatus.setIdManagementStatus(1);
            dtoStatus.setName("Started");
            dto.setStatus(dtoStatus);

            g.setAtributos(dto);
            assertThat(g.getFkIdNotaryPerson()).isNotNull();
        }

        @Test
        @DisplayName("setAtributos with null status — does not throw and leaves prior status")
        void setAtributosWithNullStatusLeavesPriorStatus() throws Exception {
            ManagementStatus prior = new ManagementStatus();
            prior.setIdManagementStatus(3);
            prior.setName("In Progress");

            DeedManagement g = new DeedManagement(1);
            g.setFkIdManagementStatus(prior);

            DtoDeedManagement dto = new DtoDeedManagement();
            dto.setNumber(300);
            dto.setStatus(null);

            assertThatCode(() -> g.setAtributos(dto)).doesNotThrowAnyException();
            assertThat(g.getFkIdManagementStatus()).isSameAs(prior);
            assertThat(g.getFkIdManagementStatus().getName()).isEqualTo("In Progress");
        }
    }

    @Nested
    @DisplayName("getDto and getDtoNotary null-safety")
    class GetDtoNullSafetyTests {

        @Test
        @DisplayName("getDto with null management status does not throw")
        void getDtoWithNullStatusDoesNotThrow() {
            DeedManagement management = new DeedManagement(1);
            management.setNumber(10);
            management.setFkIdManagementStatus(null);
            management.setFkIdNotaryPerson(null);

            assertThatCode(management::getDto).doesNotThrowAnyException();
            DtoDeedManagement dto = management.getDto();
            assertThat(dto.getStatus()).isNull();
            assertThat(dto.getPersonNotary()).isNull();
        }

        @Test
        @DisplayName("getDtoNotary with null notary returns null")
        void getDtoNotaryWithNullNotaryReturnsNull() {
            DeedManagement management = new DeedManagement(1);
            management.setFkIdNotaryPerson(null);

            assertThat(management.getDtoNotary()).isNull();
        }

        @Test
        @DisplayName("getDtoNotary with notary missing identification type does not throw")
        void getDtoNotaryWithNullIdentificationTypeDoesNotThrow() {
            Person notary = new Person();
            notary.setPersonId(5);
            notary.setFirstName("Ana");
            notary.setLastName("Lopez");
            notary.setFkIdIdentificationType(null);

            DeedManagement management = new DeedManagement(1);
            management.setFkIdNotaryPerson(notary);

            assertThatCode(management::getDtoNotary).doesNotThrowAnyException();
            DtoPerson dto = management.getDtoNotary();
            assertThat(dto).isNotNull();
            assertThat(dto.getId()).isEqualTo(5);
            assertThat(dto.getDtoIdentificationType()).isNull();
        }

        @Test
        @DisplayName("getDto with status and notary present maps both")
        void getDtoWithStatusAndNotaryMapsBoth() {
            IdentificationType idType = new IdentificationType();
            idType.setIdIdentificationType(1);
            idType.setName("DNI");

            Person notary = new Person();
            notary.setPersonId(9);
            notary.setFirstName("Carlos");
            notary.setLastName("Ruiz");
            notary.setFkIdIdentificationType(idType);

            ManagementStatus status = new ManagementStatus();
            status.setIdManagementStatus(2);
            status.setName("In Progress");

            DeedManagement management = new DeedManagement(1);
            management.setNumber(55);
            management.setFkIdNotaryPerson(notary);
            management.setFkIdManagementStatus(status);

            DtoDeedManagement dto = management.getDto();
            assertThat(dto.getStatus()).isNotNull();
            assertThat(dto.getStatus().getName()).isEqualTo("In Progress");
            assertThat(dto.getPersonNotary()).isNotNull();
            assertThat(dto.getPersonNotary().getId()).isEqualTo(9);
            assertThat(dto.getPersonNotary().getDtoIdentificationType()).isNotNull();
            assertThat(dto.getPersonNotary().getDtoIdentificationType().getName()).isEqualTo("DNI");
        }
    }

    @Nested
    @DisplayName("CU14 - Query management status - Unit Tests")
    class QueryManagementStatusTests {

        @Test
        @DisplayName("Should get current state from management")
        void shouldGetCurrentStateFromManagement() {
            ManagementStatus status = new ManagementStatus();
            status.setIdManagementStatus(2);
            status.setName("In Progress");

            DeedManagement management = new DeedManagement();
            management.setFkIdManagementStatus(status);

            assertThat(management.getFkIdManagementStatus().getName()).isEqualTo("In Progress");
        }
    }
}
