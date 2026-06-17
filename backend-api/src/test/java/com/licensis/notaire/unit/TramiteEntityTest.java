package com.licensis.notaire.unit;

import com.licensis.notaire.dto.DtoEscritura;
import com.licensis.notaire.dto.DtoGestionDeEscritura;
import com.licensis.notaire.dto.DtoInmueble;
import com.licensis.notaire.dto.DtoPresupuesto;
import com.licensis.notaire.dto.DtoTipoDeTramite;
import com.licensis.notaire.dto.DtoTramite;
import com.licensis.notaire.negocio.Escritura;
import com.licensis.notaire.negocio.GestionDeEscritura;
import com.licensis.notaire.negocio.Inmueble;
import com.licensis.notaire.negocio.Persona;
import com.licensis.notaire.negocio.Presupuesto;
import com.licensis.notaire.negocio.TipoDeTramite;
import com.licensis.notaire.negocio.Tramite;
import com.licensis.notaire.testing.RequirementCoverage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RequirementCoverage({"CU02", "CU13", "CU14"})
@DisplayName("Tramite Entity Tests")
class TramiteEntityTest {

    @Nested
    @DisplayName("Constructor and default state")
    class ConstructorTests {

        @Test
        @DisplayName("Should initialize with default constructor")
        void shouldInitializeWithDefaultConstructor() {
            Tramite tramite = new Tramite();

            assertThat(tramite.getIdTramite()).isNotNull();
            assertThat(tramite.getDocumentoPresentadoList()).isNotNull().isEmpty();
            assertThat(tramite.getPersonaList()).isNotNull().isEmpty();
            assertThat(tramite.getPresupuestoList()).isNotNull().isEmpty();
        }

        @Test
        @DisplayName("Should initialize with ID constructor")
        void shouldInitializeWithIdConstructor() {
            Tramite tramite = new Tramite(42);

            assertThat(tramite.getIdTramite()).isEqualTo(42);
        }
    }

    @Nested
    @DisplayName("Field getters and setters")
    class FieldTests {

        @Test
        @DisplayName("Should set and get observaciones")
        void shouldSetAndGetObservaciones() {
            Tramite tramite = new Tramite();
            tramite.setObservaciones("Compraventa de inmueble urbano");

            assertThat(tramite.getObservaciones()).isEqualTo("Compraventa de inmueble urbano");
        }

        @Test
        @DisplayName("Should set and get version")
        void shouldSetAndGetVersion() {
            Tramite tramite = new Tramite();
            tramite.setVersion(3);

            assertThat(tramite.getVersion()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should set and get tipo de tramite")
        void shouldSetAndGetTipoDeTramite() {
            TipoDeTramite tipo = new TipoDeTramite();
            tipo.setIdTipoTramite(10);
            tipo.setNombre("Compraventa");

            Tramite tramite = new Tramite();
            tramite.setFkIdTipoTramite(tipo);

            assertThat(tramite.getFkIdTipoTramite()).isNotNull();
            assertThat(tramite.getFkIdTipoTramite().getNombre()).isEqualTo("Compraventa");
        }

        @Test
        @DisplayName("Should set and get inmueble")
        void shouldSetAndGetInmueble() {
            Inmueble inmueble = new Inmueble();
            inmueble.setNomenclaturaCatastral("15-02-03-04-0005");

            Tramite tramite = new Tramite();
            tramite.setFkIdInmueble(inmueble);

            assertThat(tramite.getFkIdInmueble()).isNotNull();
            assertThat(tramite.getFkIdInmueble().getNomenclaturaCatastral()).isEqualTo("15-02-03-04-0005");
        }

        @Test
        @DisplayName("Should set and get presupuesto")
        void shouldSetAndGetPresupuesto() {
            Presupuesto presupuesto = new Presupuesto();
            presupuesto.setIdPresupuesto(100);

            Tramite tramite = new Tramite();
            tramite.setFkIdPresupuesto(presupuesto);

            assertThat(tramite.getFkIdPresupuesto()).isNotNull();
            assertThat(tramite.getFkIdPresupuesto().getIdPresupuesto()).isEqualTo(100);
        }

        @Test
        @DisplayName("Should set and get escritura")
        void shouldSetAndGetEscritura() {
            Escritura escritura = new Escritura();
            escritura.setNumero(2025001);

            Tramite tramite = new Tramite();
            tramite.setFkIdEscritura(escritura);

            assertThat(tramite.getFkIdEscritura()).isNotNull();
            assertThat(tramite.getFkIdEscritura().getNumero()).isEqualTo(2025001);
        }

        @Test
        @DisplayName("Should set and get gestion de escritura")
        void shouldSetAndGetGestionDeEscritura() {
            GestionDeEscritura gestion = new GestionDeEscritura();
            gestion.setNumero(5001);

            Tramite tramite = new Tramite();
            tramite.setFkIdGestion(gestion);

            assertThat(tramite.getFkIdGestion()).isNotNull();
            assertThat(tramite.getFkIdGestion().getNumero()).isEqualTo(5001);
        }

        @Test
        @DisplayName("Should set and get persona list")
        void shouldSetAndGetPersonaList() {
            Persona persona = new Persona();
            persona.setNombre("Juan");
            List<Persona> personas = new ArrayList<>();
            personas.add(persona);

            Tramite tramite = new Tramite();
            tramite.setPersonaList(personas);

            assertThat(tramite.getPersonaList()).hasSize(1);
            assertThat(tramite.getPersonaList().get(0).getNombre()).isEqualTo("Juan");
        }
    }

    @Nested
    @DisplayName("Equality and identity")
    class EqualityTests {

        @Test
        @DisplayName("Should be equal when same ID")
        void shouldBeEqualWhenSameId() {
            Tramite t1 = new Tramite(1);
            Tramite t2 = new Tramite(1);

            assertThat(t1).isEqualTo(t2);
            assertThat(t1.hashCode()).isEqualTo(t2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when different IDs")
        void shouldNotBeEqualWhenDifferentIds() {
            Tramite t1 = new Tramite(1);
            Tramite t2 = new Tramite(2);

            assertThat(t1).isNotEqualTo(t2);
        }

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            Tramite tramite = new Tramite(1);

            assertThat(tramite).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Should not be equal to different type")
        void shouldNotBeEqualToDifferentType() {
            Tramite tramite = new Tramite(1);

            assertThat(tramite).isNotEqualTo("not a tramite");
        }

        @Test
        @DisplayName("toString should include ID")
        void toStringShouldIncludeId() {
            Tramite tramite = new Tramite(99);

            assertThat(tramite.toString()).contains("99");
        }

        @Test
        @DisplayName("hashCode should be zero when ID is null")
        void hashCodeShouldBeZeroWhenIdIsNull() {
            Tramite tramite = new Tramite();
            tramite.setIdTramite(null);

            assertThat(tramite.hashCode()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("setAtributos branches")
    class SetAtributosTests {

        private DtoTramite baseDto() {
            DtoTipoDeTramite tipoDto = new DtoTipoDeTramite();
            tipoDto.setIdTipoTramite(1);
            tipoDto.setNombre("Compraventa");
            DtoTramite dto = new DtoTramite();
            dto.setIdTramite(10);
            dto.setObservaciones("obs");
            dto.setTiposDeTramite(tipoDto);
            return dto;
        }

        @Test
        @DisplayName("setAtributos with all optional fields null — covers null branches")
        void setAtributosAllNullOptional() {
            DtoTramite dto = baseDto();
            dto.setInmueble(null);

            Tramite tramite = new Tramite();
            tramite.setAtributos(dto);

            assertThat(tramite.getIdTramite()).isEqualTo(10);
            assertThat(tramite.getFkIdInmueble()).isNull();
            assertThat(tramite.getFkIdEscritura()).isNull();
            assertThat(tramite.getFkIdGestion()).isNull();
            assertThat(tramite.getFkIdPresupuesto()).isNull();
        }

        @Test
        @DisplayName("setAtributos with non-null inmueble — covers non-null branch")
        void setAtributosWithInmueble() {
            DtoTramite dto = baseDto();
            DtoInmueble dtoInmueble = new DtoInmueble();
            dtoInmueble.setIdInmueble(5);
            dtoInmueble.setDomicilio("Calle Test 123");
            dto.setInmueble(dtoInmueble);

            Tramite tramite = new Tramite();
            tramite.setAtributos(dto);

            assertThat(tramite.getFkIdInmueble()).isNotNull();
        }

        @Test
        @DisplayName("setAtributos with non-null escritura — covers non-null branch")
        void setAtributosWithEscritura() {
            DtoTramite dto = baseDto();
            DtoEscritura dtoEscritura = new DtoEscritura();
            dtoEscritura.setIdEscritura(7);
            dtoEscritura.setNumero(2025001);
            dto.setEscritura(dtoEscritura);

            Tramite tramite = new Tramite();
            tramite.setAtributos(dto);

            assertThat(tramite.getFkIdEscritura()).isNotNull();
        }

        @Test
        @DisplayName("setAtributos with non-null gestion — covers non-null branch")
        void setAtributosWithGestion() {
            DtoTramite dto = baseDto();
            DtoGestionDeEscritura dtoGestion = new DtoGestionDeEscritura();
            dtoGestion.setIdGestion(3);
            dto.setGestionDeEscritura(dtoGestion);

            Tramite tramite = new Tramite();
            tramite.setAtributos(dto);

            assertThat(tramite.getFkIdGestion()).isNotNull();
        }

        @Test
        @DisplayName("setAtributos with non-null presupuesto — covers non-null branch")
        void setAtributosWithPresupuesto() {
            DtoTramite dto = baseDto();
            DtoPresupuesto dtoPresupuesto = new DtoPresupuesto();
            dtoPresupuesto.setIdPresupuesto(20);
            dtoPresupuesto.setNumero(12345);
            dtoPresupuesto.setVersion(0);
            dto.setPresupuesto(dtoPresupuesto);

            Tramite tramite = new Tramite();
            tramite.setAtributos(dto);

            assertThat(tramite.getFkIdPresupuesto()).isNotNull();
        }
    }

    @Nested
    @DisplayName("getDto branches")
    class GetDtoTests {

        private Tramite baseTramite() {
            Tramite tramite = new Tramite(5);
            TipoDeTramite tipo = new TipoDeTramite(1);
            tipo.setNombre("Compraventa");
            tramite.setFkIdTipoTramite(tipo);
            return tramite;
        }

        @Test
        @DisplayName("getDto with all optional refs null — covers null branches")
        void getDtoAllNullOptional() {
            Tramite tramite = baseTramite();

            var dto = tramite.getDto();

            assertThat(dto.getIdTramite()).isEqualTo(5);
            assertThat(dto.getEscritura()).isNull();
            assertThat(dto.getGestion()).isNull();
            assertThat(dto.getInmueble()).isNull();
            assertThat(dto.getPresupuesto()).isNull();
        }

        @Test
        @DisplayName("getDto with escritura set — covers non-null escritura branch")
        void getDtoWithEscritura() {
            Tramite tramite = baseTramite();
            Escritura escritura = new Escritura(7);
            escritura.setNumero(2025001);
            tramite.setFkIdEscritura(escritura);

            var dto = tramite.getDto();

            assertThat(dto.getEscritura()).isNotNull();
            assertThat(dto.getEscritura().getIdEscritura()).isEqualTo(7);
        }

        @Test
        @DisplayName("getDto with inmueble set — covers non-null inmueble branch")
        void getDtoWithInmueble() {
            Tramite tramite = baseTramite();
            Inmueble inmueble = new Inmueble(3);
            inmueble.setDomicilio("Calle Test 456");
            tramite.setFkIdInmueble(inmueble);

            var dto = tramite.getDto();

            assertThat(dto.getInmueble()).isNotNull();
        }

        @Test
        @DisplayName("getDto with presupuesto set — covers non-null presupuesto branch")
        void getDtoWithPresupuesto() {
            Tramite tramite = baseTramite();
            Presupuesto presupuesto = new Presupuesto(15);
            tramite.setFkIdPresupuesto(presupuesto);

            var dto = tramite.getDto();

            assertThat(dto.getPresupuesto()).isNotNull();
            assertThat(dto.getPresupuesto().getIdPresupuesto()).isEqualTo(15);
        }

        @Test
        @DisplayName("getDto with gestion set — covers non-null gestion branch")
        void getDtoWithGestion() {
            Tramite tramite = baseTramite();
            GestionDeEscritura gestion = new GestionDeEscritura();
            gestion.setIdGestion(9);
            gestion.setNumero(5001);
            Persona escribano = new Persona(3);
            escribano.setRegistroEscribano(1001);
            gestion.setFkIdPersonaEscribano(escribano);
            tramite.setFkIdGestion(gestion);

            var dto = tramite.getDto();

            assertThat(dto.getGestion()).isNotNull();
            assertThat(dto.getGestion().getIdGestion()).isEqualTo(9);
        }
    }
}
