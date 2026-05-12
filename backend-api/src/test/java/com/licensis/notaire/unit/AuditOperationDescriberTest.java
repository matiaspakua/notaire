package com.licensis.notaire.unit;

import com.licensis.notaire.audit.AuditOperationDescriber;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AuditOperationDescriber unit tests")
class AuditOperationDescriberTest {

    static class SampleController {

        @GetMapping
        public void listAll() {
            // no-op
        }

        @GetMapping("/{id}")
        public void getById() {
            // no-op
        }

        @PostMapping
        public void create() {
            // no-op
        }

        @PutMapping("/{id}")
        public void update() {
            // no-op
        }

        @PatchMapping("/{id}")
        public void patchUpdate() {
            // no-op
        }

        @DeleteMapping("/{id}")
        public void delete() {
            // no-op
        }

        public void login() {
            // no-op
        }

        public void logout() {
            // no-op
        }
    }

    private Method method(String name) throws NoSuchMethodException {
        return SampleController.class.getDeclaredMethod(name);
    }

    @Test
    @DisplayName("Should describe GET listing as listado consulta")
    void shouldDescribeGetListAsListadoConsulta() throws NoSuchMethodException {
        String result = AuditOperationDescriber.describe(method("listAll"), new Object[0], "Escrituras");
        assertThat(result).isEqualTo("Consulta de listado de escrituras");
    }

    @Test
    @DisplayName("Should describe GET by id as consulta con ID")
    void shouldDescribeGetByIdAsConsultaConId() throws NoSuchMethodException {
        String result = AuditOperationDescriber.describe(method("getById"), new Object[]{7}, "Escrituras");
        assertThat(result).isEqualTo("Consulta de escritura con ID 7");
    }

    @Test
    @DisplayName("Should describe POST as creacion")
    void shouldDescribePostAsCreacion() throws NoSuchMethodException {
        String result = AuditOperationDescriber.describe(method("create"), new Object[]{new Object()}, "Personas");
        assertThat(result).isEqualTo("Creación de nuevo persona");
    }

    @Test
    @DisplayName("Should describe PUT with id as actualizacion con ID")
    void shouldDescribePutAsActualizacion() throws NoSuchMethodException {
        String result = AuditOperationDescriber.describe(method("update"), new Object[]{42, new Object()}, "Presupuestos");
        assertThat(result).isEqualTo("Actualización de presupuesto con ID 42");
    }

    @Test
    @DisplayName("Should describe PATCH with id as actualizacion con ID")
    void shouldDescribePatchAsActualizacion() throws NoSuchMethodException {
        String result = AuditOperationDescriber.describe(method("patchUpdate"), new Object[]{10}, "Conceptos");
        assertThat(result).isEqualTo("Actualización de concepto con ID 10");
    }

    @Test
    @DisplayName("Should describe DELETE as eliminacion con ID")
    void shouldDescribeDeleteAsEliminacionConId() throws NoSuchMethodException {
        String result = AuditOperationDescriber.describe(method("delete"), new Object[]{99}, "Pagos");
        assertThat(result).isEqualTo("Eliminación de pago con ID 99");
    }

    @Test
    @DisplayName("Should describe login method as inicio de sesion")
    void shouldDescribeLoginAsInicioDeSesion() throws NoSuchMethodException {
        String result = AuditOperationDescriber.describe(method("login"), new Object[0], "Usuarios");
        assertThat(result).isEqualTo("Inicio de sesión del usuario");
    }

    @Test
    @DisplayName("Should describe logout method as cierre de sesion")
    void shouldDescribeLogoutAsCierreDeSesion() throws NoSuchMethodException {
        String result = AuditOperationDescriber.describe(method("logout"), new Object[0], "Usuarios");
        assertThat(result).isEqualTo("Cierre de sesión del usuario");
    }

    @Test
    @DisplayName("Should default to GET listing when method is null")
    void shouldDefaultToGetListingWhenMethodNull() {
        String result = AuditOperationDescriber.describe(null, new Object[0], "Escrituras");
        assertThat(result).isEqualTo("Consulta de listado de escrituras");
    }

    @Test
    @DisplayName("Should default to listing when module is null")
    void shouldDefaultToListingWhenModuleNull() throws NoSuchMethodException {
        String result = AuditOperationDescriber.describe(method("listAll"), new Object[0], null);
        assertThat(result).isEqualTo("Consulta de listado de registro");
    }

    @Test
    @DisplayName("Should describe PUT without id when no id argument")
    void shouldDescribePutWithoutIdWhenNoIdArgument() throws NoSuchMethodException {
        String result = AuditOperationDescriber.describe(method("update"), new Object[]{null, new Object()}, "Personas");
        assertThat(result).isEqualTo("Actualización de persona");
    }

    @Test
    @DisplayName("Should describe DELETE without id when no id argument")
    void shouldDescribeDeleteWithoutIdWhenNoIdArgument() throws NoSuchMethodException {
        String result = AuditOperationDescriber.describe(method("delete"), new Object[]{}, "Personas");
        assertThat(result).isEqualTo("Eliminación de persona");
    }

    @Test
    @DisplayName("Should handle plural ending in -nes correctly")
    void shouldHandlePluralEndingInNes() throws NoSuchMethodException {
        String result = AuditOperationDescriber.describe(method("create"), new Object[]{}, "Gestiones");
        assertThat(result).isEqualTo("Creación de nuevo gestion");
    }

    @Test
    @DisplayName("Should handle module without plural ending")
    void shouldHandleModuleWithoutPluralEnding() throws NoSuchMethodException {
        String result = AuditOperationDescriber.describe(method("create"), new Object[]{}, "Reportes");
        assertThat(result).isEqualTo("Creación de nuevo reporte");
    }

    @Test
    @DisplayName("Should handle blank module")
    void shouldHandleBlankModule() throws NoSuchMethodException {
        String result = AuditOperationDescriber.describe(method("listAll"), new Object[0], "");
        assertThat(result).isEqualTo("Consulta de listado de registro");
    }
}
