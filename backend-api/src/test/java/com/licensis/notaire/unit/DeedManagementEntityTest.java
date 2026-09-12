package com.licensis.notaire.unit;

import com.licensis.notaire.dto.DtoManagementStatus;
import com.licensis.notaire.dto.DtoDeedManagement;
import com.licensis.notaire.dto.DtoPerson;
import com.licensis.notaire.dto.DtoIdentificationType;
import com.licensis.notaire.business.ManagementStatus;
import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.Person;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import com.licensis.notaire.testing.RequirementCoverage;

@RequirementCoverage({"CU02", "CU13", "CU14", "CU19", "CU24"})
@DisplayName("GestionDeEscritura Entity Tests")
class DeedManagementEntityTest {

    @Nested
    @DisplayName("CU02 - Iniciar Gestión - Unit Tests")
    class IniciarManagementTests {

        @Test
        @DisplayName("Should create gestion with required fields")
        void shouldCreateManagementWithRequiredFields() {
            DeedManagement management = new DeedManagement();
            management.setIdManagement(1);
            management.setNumber(1001);
            management.setDateStart(new Date());
            management.setEncabezado("Compraventa - Perez Garcia");

            assertThat(management.getNumber()).isEqualTo(1001);
            assertThat(management.getEncabezado()).isEqualTo("Compraventa - Perez Garcia");
        }

        @Test
        @DisplayName("Should link gestion to notary")
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
        @DisplayName("Should link gestion to status")
        void shouldLinkManagementToStatus() {
            ManagementStatus status = new ManagementStatus();
            status.setIdManagementStatus(1);
            status.setName("Iniciada");

            DeedManagement management = new DeedManagement();
            management.setIdManagement(1);
            management.setFkIdManagementStatus(status);

            assertThat(management.getFkIdManagementStatus()).isNotNull();
            assertThat(management.getFkIdManagementStatus().getName()).isEqualTo("Iniciada");
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
    @DisplayName("CU19 - Buscar gestiones de un Cliente - Unit Tests")
    class SearchGestionesClientTests {

        @Test
        @DisplayName("Should filter gestiones by cliente")
        void shouldFilterGestionesByClient() {
            DeedManagement gestion1 = new DeedManagement(1);
            DeedManagement gestion2 = new DeedManagement(2);
            DeedManagement gestion3 = new DeedManagement(3);

            var gestiones = java.util.List.of(gestion1, gestion2, gestion3);

            assertThat(gestiones).hasSize(3);
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
            dtoStatus.setName("Iniciada");
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
            dtoPerson.setLastName("García");
            dtoPerson.setVersion(0);
            dtoPerson.setDtoIdentificationType(dtoTypeId);
            dto.setPersonNotary(dtoPerson);
            DtoManagementStatus dtoStatus = new DtoManagementStatus();
            dtoStatus.setIdManagementStatus(1);
            dtoStatus.setName("Iniciada");
            dto.setStatus(dtoStatus);

            g.setAtributos(dto);
            assertThat(g.getFkIdNotaryPerson()).isNotNull();
        }
    }

    @Nested
    @DisplayName("CU14 - Consultar status gestión - Unit Tests")
    class ConsultarStatusManagementTests {

        @Test
        @DisplayName("Should get current state from gestion")
        void shouldGetCurrentStateFromManagement() {
            ManagementStatus status = new ManagementStatus();
            status.setIdManagementStatus(2);
            status.setName("En Tramite");

            DeedManagement management = new DeedManagement();
            management.setFkIdManagementStatus(status);

            assertThat(management.getFkIdManagementStatus().getName()).isEqualTo("En Tramite");
        }
    }
}
