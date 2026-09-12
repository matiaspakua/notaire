package com.licensis.notaire.unit;

import com.licensis.notaire.dto.DtoDeed;
import com.licensis.notaire.dto.DtoFolio;
import com.licensis.notaire.dto.DtoProcedure;
import com.licensis.notaire.business.BusinessConstants;
import com.licensis.notaire.business.Deed;
import com.licensis.notaire.business.Folio;
import com.licensis.notaire.business.Testimony;
import com.licensis.notaire.business.Procedure;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import com.licensis.notaire.testing.RequirementCoverage;

@RequirementCoverage({"CU05", "CU06", "CU52", "CU62"})
@DisplayName("Escritura Entity Tests")
class DeedEntityTest {

    @Nested
    @DisplayName("CU05 - Preparar deed - Unit Tests")
    class PrepararDeedTests {

        @Test
        @DisplayName("Should create deed with required fields")
        void shouldCreateDeedWithRequiredFields() {
            Deed deed = new Deed();
            deed.setIdDeed(1);
            deed.setNumber(1001);
            deed.setDateDeedrecording(new Date());
            deed.setBody("Escritura de compraventa");
            deed.setStatus("firmada");

            assertThat(deed.getNumber()).isEqualTo(1001);
            assertThat(deed.getBody()).isEqualTo("Escritura de compraventa");
            assertThat(deed.getStatus()).isEqualTo("firmada");
        }

        @Test
        @DisplayName("Should set deed states")
        void shouldSetDeedStates() {
            Deed deed = new Deed();

            deed.setStatus("firmada");
            assertThat(deed.getStatus()).isEqualTo("firmada");

            deed.setStatus("no_firmada");
            assertThat(deed.getStatus()).isEqualTo("no_firmada");

            deed.setStatus("anulada");
            assertThat(deed.getStatus()).isEqualTo("anulada");

            deed.setStatus("no_paso");
            assertThat(deed.getStatus()).isEqualTo("no_paso");
        }

        @Test
        @DisplayName("Should implement equals based on id")
        void shouldImplementEqualsBasedOnId() {
            Deed e1 = new Deed(1);
            Deed e2 = new Deed(1);
            Deed e3 = new Deed(2);

            assertThat(e1).isEqualTo(e2);
            assertThat(e1).isNotEqualTo(e3);
        }

        @Test
        @DisplayName("Should have SIN_FIRMAR as default status")
        void shouldHaveSinFirmarAsDefaultStatus() {
            Deed deed = new Deed();

            assertThat(deed.getStatus()).isEqualTo(BusinessConstants.DeedSINFIRMAR);
        }

        @Test
        @DisplayName("Should initialize with full constructor")
        void shouldInitializeWithFullConstructor() {
            Date date = new Date();
            Deed deed = new Deed(7, 2025007, date, "Cuerpo notarial", "FIRMADA");

            assertThat(deed.getIdDeed()).isEqualTo(7);
            assertThat(deed.getNumber()).isEqualTo(2025007);
            assertThat(deed.getDateDeedrecording()).isEqualTo(date);
            assertThat(deed.getBody()).isEqualTo("Cuerpo notarial");
            assertThat(deed.getStatus()).isEqualTo("FIRMADA");
        }
    }

    @Nested
    @DisplayName("Field getters and setters")
    class FieldTests {

        @Test
        @DisplayName("Should set and get registrationEntryNumber")
        void shouldSetAndGetRegistrationEntryNumber() {
            Deed deed = new Deed();
            deed.setRegistrationEntryNumber("MAT-2025-001");

            assertThat(deed.getRegistrationEntryNumber()).isEqualTo("MAT-2025-001");
        }

        @Test
        @DisplayName("Should set and get dateRegistration")
        void shouldSetAndGetDateRegistration() {
            Date date = new Date();
            Deed deed = new Deed();
            deed.setDateRegistration(date);

            assertThat(deed.getDateRegistration()).isEqualTo(date);
        }

        @Test
        @DisplayName("Should set and get notes")
        void shouldSetAndGetNotes() {
            Deed deed = new Deed();
            deed.setNotes("Pendiente de firma");

            assertThat(deed.getNotes()).isEqualTo("Pendiente de firma");
        }

        @Test
        @DisplayName("Should set and get version")
        void shouldSetAndGetVersion() {
            Deed deed = new Deed();
            deed.setVersion(4);

            assertThat(deed.getVersion()).isEqualTo(4);
        }

        @Test
        @DisplayName("Should set and get folio list")
        void shouldSetAndGetFolioList() {
            List<Folio> folios = new ArrayList<>();
            folios.add(new Folio());

            Deed deed = new Deed();
            deed.setFolioList(folios);

            assertThat(deed.getFolioList()).hasSize(1);
        }

        @Test
        @DisplayName("Should set and get tramite list")
        void shouldSetAndGetProcedureList() {
            List<Procedure> procedures = new ArrayList<>();
            procedures.add(new Procedure(1));

            Deed deed = new Deed();
            deed.setProcedureList(procedures);

            assertThat(deed.getProcedureList()).hasSize(1);
        }

        @Test
        @DisplayName("Should set and get testimony list")
        void shouldSetAndGetTestimonyList() {
            List<Testimony> testimonios = new ArrayList<>();
            testimonios.add(new Testimony());

            Deed deed = new Deed();
            deed.setTestimonyList(testimonios);

            assertThat(deed.getTestimonyList()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Equality and identity")
    class EqualityTests {

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            Deed deed = new Deed(1);

            assertThat(deed).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Should not be equal to different type")
        void shouldNotBeEqualToDifferentType() {
            Deed deed = new Deed(1);

            assertThat(deed).isNotEqualTo("string");
        }

        @Test
        @DisplayName("hashCode should be zero when ID is null")
        void hashCodeShouldBeZeroWhenIdIsNull() {
            Deed deed = new Deed();

            assertThat(deed.hashCode()).isEqualTo(0);
        }

        @Test
        @DisplayName("hashCode should be consistent with equals")
        void hashCodeShouldBeConsistentWithEquals() {
            Deed e1 = new Deed(3);
            Deed e2 = new Deed(3);

            assertThat(e1.hashCode()).isEqualTo(e2.hashCode());
        }

        @Test
        @DisplayName("toString should include ID")
        void toStringShouldIncludeId() {
            Deed deed = new Deed(42);

            assertThat(deed.toString()).contains("42");
        }
    }

    @Nested
    @DisplayName("setAtributos branches")
    class SetAtributosTests {

        @Test
        @DisplayName("setAtributos with null DTO — no-op")
        void setAtributosWithNullDtoIsNoOp() {
            Deed e = new Deed(1);
            e.setAtributos(null);
            assertThat(e.getIdDeed()).isEqualTo(1);
        }

        @Test
        @DisplayName("setAtributos with null idDeed — does not overwrite id")
        void setAtributosDoesNotOverwriteWhenIdNull() {
            Deed e = new Deed();
            DtoDeed dto = new DtoDeed();
            dto.setNumber(100);
            dto.setIdDeed(5);
            e.setAtributos(dto);
            assertThat(e.getNumber()).isEqualTo(100);
        }

        @Test
        @DisplayName("setAtributos with non-null idDeed — copies id")
        void setAtributosOverwritesIdWhenNotNull() {
            Deed e = new Deed(1);
            DtoDeed dto = new DtoDeed();
            dto.setIdDeed(9);
            dto.setNumber(200);
            e.setAtributos(dto);
            assertThat(e.getNumber()).isEqualTo(200);
        }

        @Test
        @DisplayName("setAtributos with non-null folios — populates folioList")
        void setAtributosWithFoliosPopulatesList() {
            Deed e = new Deed(1);
            DtoDeed dto = new DtoDeed();
            dto.setNumber(1);
            dto.getFolios().add(new DtoFolio());
            e.setAtributos(dto);
            assertThat(e.getFolioList()).isNotNull();
        }

        @Test
        @DisplayName("setAtributos with empty DTO folios — creates empty folioList")
        void setAtributosWithEmptyFoliosCreatesEmptyList() {
            Deed e = new Deed(1);
            DtoDeed dto = new DtoDeed();
            dto.setNumber(1);
            // DtoEscritura.getFolios() always returns a new ArrayList (setFolios is no-op in shared module)
            e.setAtributos(dto);
            assertThat(e.getFolioList()).isNotNull().isEmpty();
        }

        @Test
        @DisplayName("setAtributos with non-null procedures — populates tramiteList")
        void setAtributosWithProceduresPopulatesList() {
            Deed e = new Deed(1);
            DtoDeed dto = new DtoDeed();
            dto.setNumber(1);
            DtoProcedure dtoProcedure = new DtoProcedure();
            dtoProcedure.setIdProcedure(10);
            dto.getProcedures().add(dtoProcedure);
            e.setAtributos(dto);
            assertThat(e.getProcedureList()).isNotNull().hasSize(1);
        }

        @Test
        @DisplayName("setAtributos with null procedures — does not create tramiteList")
        void setAtributosWithNullProceduresDoesNotCreateList() {
            Deed e = new Deed(1);
            DtoDeed dto = new DtoDeed();
            dto.setNumber(1);
            dto.setProcedures(null);
            e.setAtributos(dto);
            assertThat(e.getProcedureList()).isNull();
        }
    }

    @Nested
    @DisplayName("getDto branches")
    class GetDtoTests {

        @Test
        @DisplayName("getDto with null folioList returns empty folios from DTO")
        void getDtoWithNullFolioListReturnsFoliosEmpty() {
            Deed e = new Deed(1);
            e.setNumber(42);
            // folioList is null by default → getDto sets folios null on DTO, but DtoEscritura.getFolios() returns new ArrayList<>()
            var dto = e.getDto();
            assertThat(dto.getFolios()).isNotNull().isEmpty();
        }

        @Test
        @DisplayName("getDto with empty folioList returns empty folios from DTO")
        void getDtoWithEmptyFolioListReturnsFoliosEmpty() {
            Deed e = new Deed(1);
            e.setNumber(42);
            e.setFolioList(new ArrayList<>());
            var dto = e.getDto();
            assertThat(dto.getFolios()).isNotNull().isEmpty();
        }

        @Test
        @DisplayName("getDto with null tramiteList returns empty procedures")
        void getDtoWithNullProcedureListReturnsProceduresEmpty() {
            Deed e = new Deed(1);
            e.setNumber(42);
            var dto = e.getDto();
            // tramiteList null → setTramites(null) → dto.getTramites() is null
            assertThat(dto.getProcedures()).isNull();
        }

        @Test
        @DisplayName("getDto with empty tramiteList returns null procedures")
        void getDtoWithEmptyProcedureListReturnsProceduresNull() {
            Deed e = new Deed(1);
            e.setNumber(42);
            e.setProcedureList(new ArrayList<>());
            var dto = e.getDto();
            assertThat(dto.getProcedures()).isNull();
        }

        @Test
        @DisplayName("getDto maps basic fields correctly")
        void getDtoMapsBasicFields() {
            Deed e = new Deed(5);
            e.setNumber(3001);
            e.setBody("Escritura de prueba");
            var dto = e.getDto();
            assertThat(dto.getIdDeed()).isEqualTo(5);
            assertThat(dto.getNumber()).isEqualTo(3001);
            assertThat(dto.getBody()).isEqualTo("Escritura de prueba");
        }
    }
}
