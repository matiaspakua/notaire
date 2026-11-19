package com.licensis.notaire.unit;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.licensis.notaire.adapter.out.persistence.procedure.ProcedureFolderLegacyMapper;
import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.business.ProcedureFolder;

@DisplayName("Procedure folder mapping lives outside the entity")
class ProcedureFolderLegacyMapperTest {

    @Test
    @DisplayName("shouldCopyFolderFieldsOntoTheDto")
    void shouldCopyFolderFieldsOntoTheDto() {
        DeedManagement management = new DeedManagement();
        management.setIdManagement(4);
        Procedure procedure = new Procedure();
        procedure.setIdProcedure(8);
        ProcedureFolder folder = new ProcedureFolder();
        folder.setIdFolder(3);
        folder.setNumber(12);
        folder.setStatus("WAITING");
        folder.setWaitReason("signature");
        folder.setFkIdManagement(management);
        folder.setFkIdProcedure(procedure);

        var dto = ProcedureFolderLegacyMapper.toDto(folder);

        assertThat(dto.getIdFolder()).isEqualTo(3);
        assertThat(dto.getNumber()).isEqualTo(12);
        assertThat(dto.getStatus()).isEqualTo("WAITING");
        assertThat(dto.getWaitReason()).isEqualTo("signature");
        assertThat(dto.getIdManagement()).isEqualTo(4);
        assertThat(dto.getIdProcedure()).isEqualTo(8);
    }

    @Test
    @DisplayName("shouldLeaveLinksEmptyWhenTheFolderHasNone")
    void shouldLeaveLinksEmptyWhenTheFolderHasNone() {
        ProcedureFolder folder = new ProcedureFolder();
        folder.setIdFolder(1);

        var dto = ProcedureFolderLegacyMapper.toDto(folder);

        assertThat(dto.getIdManagement()).isNull();
        assertThat(dto.getIdProcedure()).isNull();
    }

    @Test
    @DisplayName("shouldKeepDtoTypesOutOfProcedureFolder")
    void shouldKeepDtoTypesOutOfProcedureFolder() {
        assertThat(Arrays.stream(ProcedureFolder.class.getDeclaredMethods()).map(method -> method.getName()))
                .doesNotContain("getDto");
    }
}
