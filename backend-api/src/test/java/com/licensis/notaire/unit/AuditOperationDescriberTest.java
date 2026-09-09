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
    @DisplayName("Should describe GET listing as a listing query")
    void shouldDescribeGetListAsListingQuery() throws NoSuchMethodException {
        String result = AuditOperationDescriber.describe(method("listAll"), new Object[0], "Deeds");
        assertThat(result).isEqualTo("Listing query of deeds");
    }

    @Test
    @DisplayName("Should describe GET by id as a lookup with ID")
    void shouldDescribeGetByIdAsLookupWithId() throws NoSuchMethodException {
        String result = AuditOperationDescriber.describe(method("getById"), new Object[]{7}, "Deeds");
        assertThat(result).isEqualTo("Lookup of deed with ID 7");
    }

    @Test
    @DisplayName("Should describe POST as creation")
    void shouldDescribePostAsCreation() throws NoSuchMethodException {
        String result = AuditOperationDescriber.describe(method("create"), new Object[]{new Object()}, "People");
        assertThat(result).isEqualTo("Creation of new people");
    }

    @Test
    @DisplayName("Should describe PUT with id as update with ID")
    void shouldDescribePutAsUpdateWithId() throws NoSuchMethodException {
        String result = AuditOperationDescriber.describe(method("update"), new Object[]{42, new Object()}, "Budgets");
        assertThat(result).isEqualTo("Update of budget with ID 42");
    }

    @Test
    @DisplayName("Should describe PATCH with id as update with ID")
    void shouldDescribePatchAsUpdateWithId() throws NoSuchMethodException {
        String result = AuditOperationDescriber.describe(method("patchUpdate"), new Object[]{10}, "Concepts");
        assertThat(result).isEqualTo("Update of concept with ID 10");
    }

    @Test
    @DisplayName("Should describe DELETE as deletion with ID")
    void shouldDescribeDeleteAsDeletionWithId() throws NoSuchMethodException {
        String result = AuditOperationDescriber.describe(method("delete"), new Object[]{99}, "Payments");
        assertThat(result).isEqualTo("Deletion of payment with ID 99");
    }

    @Test
    @DisplayName("Should describe login method as user login")
    void shouldDescribeLoginAsUserLogin() throws NoSuchMethodException {
        String result = AuditOperationDescriber.describe(method("login"), new Object[0], "Users");
        assertThat(result).isEqualTo("User login");
    }

    @Test
    @DisplayName("Should describe logout method as user logout")
    void shouldDescribeLogoutAsUserLogout() throws NoSuchMethodException {
        String result = AuditOperationDescriber.describe(method("logout"), new Object[0], "Users");
        assertThat(result).isEqualTo("User logout");
    }

    @Test
    @DisplayName("Should default to GET listing when method is null")
    void shouldDefaultToGetListingWhenMethodNull() {
        String result = AuditOperationDescriber.describe(null, new Object[0], "Deeds");
        assertThat(result).isEqualTo("Listing query of deeds");
    }

    @Test
    @DisplayName("Should default to listing when module is null")
    void shouldDefaultToListingWhenModuleNull() throws NoSuchMethodException {
        String result = AuditOperationDescriber.describe(method("listAll"), new Object[0], null);
        assertThat(result).isEqualTo("Listing query of record");
    }

    @Test
    @DisplayName("Should describe PUT without id when no id argument")
    void shouldDescribePutWithoutIdWhenNoIdArgument() throws NoSuchMethodException {
        String result = AuditOperationDescriber.describe(method("update"), new Object[]{null, new Object()}, "People");
        assertThat(result).isEqualTo("Update of people");
    }

    @Test
    @DisplayName("Should describe DELETE without id when no id argument")
    void shouldDescribeDeleteWithoutIdWhenNoIdArgument() throws NoSuchMethodException {
        String result = AuditOperationDescriber.describe(method("delete"), new Object[]{}, "People");
        assertThat(result).isEqualTo("Deletion of people");
    }

    @Test
    @DisplayName("Should handle plural ending in -ies correctly")
    void shouldHandlePluralEndingInIes() throws NoSuchMethodException {
        String result = AuditOperationDescriber.describe(method("create"), new Object[]{}, "Copies");
        assertThat(result).isEqualTo("Creation of new copy");
    }

    @Test
    @DisplayName("Should handle plural ending in -ses correctly")
    void shouldHandlePluralEndingInSes() throws NoSuchMethodException {
        String result = AuditOperationDescriber.describe(method("create"), new Object[]{}, "ManagementStatuses");
        assertThat(result).isEqualTo("Creation of new managementstatus");
    }

    @Test
    @DisplayName("Should handle module without plural ending")
    void shouldHandleModuleWithoutPluralEnding() throws NoSuchMethodException {
        String result = AuditOperationDescriber.describe(method("create"), new Object[]{}, "History");
        assertThat(result).isEqualTo("Creation of new history");
    }

    @Test
    @DisplayName("Should handle blank module")
    void shouldHandleBlankModule() throws NoSuchMethodException {
        String result = AuditOperationDescriber.describe(method("listAll"), new Object[0], "");
        assertThat(result).isEqualTo("Listing query of record");
    }
}
