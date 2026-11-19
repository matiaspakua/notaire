package com.licensis.notaire.unit;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.licensis.notaire.adapter.out.persistence.procedure.ProcedureLegacyMapper;
import com.licensis.notaire.business.Budget;
import com.licensis.notaire.business.Deed;
import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.business.ProcedureType;
import com.licensis.notaire.business.Property;

class ProcedureLegacyMapperTest {

    private static Procedure base() {
        Procedure procedure = new Procedure(5);
        ProcedureType type = new ProcedureType(1);
        type.setName("Compraventa");
        procedure.setFkIdProcedureType(type);
        return procedure;
    }

    @Test
    @DisplayName("toDto copies the type and leaves optional links empty")
    void toDtoLeavesOptionalLinksEmpty() {
        var dto = ProcedureLegacyMapper.toDto(base());

        assertThat(dto.getIdProcedure()).isEqualTo(5);
        assertThat(dto.getProcedureType().getName()).isEqualTo("Compraventa");
        assertThat(dto.getDeed()).isNull();
        assertThat(dto.getManagement()).isNull();
        assertThat(dto.getProperty()).isNull();
        assertThat(dto.getBudget()).isNull();
    }

    @Test
    @DisplayName("toDto copies deed, property, budget, and a short management")
    void toDtoCopiesPresentLinks() {
        Procedure procedure = base();
        Deed deed = new Deed(7);
        deed.setNumber(2025001);
        procedure.setFkIdDeed(deed);
        Property property = new Property(3);
        property.setAddress("Street Test 456");
        procedure.setFkIdProperty(property);
        procedure.setFkIdBudget(new Budget(15));
        DeedManagement management = new DeedManagement();
        management.setIdManagement(9);
        management.setNumber(5001);
        Person notary = new Person(3);
        notary.setNotaryRegistrationNumber(1001);
        management.setFkIdNotaryPerson(notary);
        procedure.setFkIdManagement(management);

        var dto = ProcedureLegacyMapper.toDto(procedure);

        assertThat(dto.getDeed().getIdDeed()).isEqualTo(7);
        assertThat(dto.getDeed().getNumber()).isEqualTo(2025001);
        assertThat(dto.getProperty().getAddress()).isEqualTo("Street Test 456");
        assertThat(dto.getBudget().getIdBudget()).isEqualTo(15);
        assertThat(dto.getManagement().getIdManagement()).isEqualTo(9);
        assertThat(dto.getManagement().getPersonNotary().getId()).isEqualTo(3);
        assertThat(procedure.getSubmittedDocumentList()).isEmpty();
    }

    @Test
    @DisplayName("the procedure entity no longer builds its transport DTO")
    void entityDoesNotMapItself() throws Exception {
        String source = Files.readString(Path.of("src/main/java/com/licensis/notaire/business/Procedure.java"));
        assertThat(source).doesNotContain("DtoProcedure dto");
    }
}
