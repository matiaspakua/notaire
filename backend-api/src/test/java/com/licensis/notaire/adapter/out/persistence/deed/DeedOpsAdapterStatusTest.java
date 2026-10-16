package com.licensis.notaire.adapter.out.persistence.deed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.licensis.notaire.application.port.out.deed.DeedOpsPort.DeedCommand;
import com.licensis.notaire.application.port.out.deed.DeedOpsPort.SaveResult;
import com.licensis.notaire.application.usecase.deed.DeedService;
import com.licensis.notaire.application.usecase.deed.DeedSigningService;
import com.licensis.notaire.business.Deed;
import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.repository.FolioRepository;

/**
 * Status vocabulary and CU05 default for deeds (issue #1811).
 */
@DisplayName("DeedOpsAdapter status")
class DeedOpsAdapterStatusTest {

    private final DeedService deedService = mock(DeedService.class);
    private final DeedSigningService signingService = mock(DeedSigningService.class);
    private final FolioRepository folioRepository = mock(FolioRepository.class);
    private final DeedOpsAdapter adapter = new DeedOpsAdapter(deedService, signingService, folioRepository);

    @Test
    @DisplayName("canonicalStatus accepts known values case-insensitively and rejects others")
    void canonicalStatusVocabulary() {
        assertThat(DeedOpsAdapter.canonicalStatus(null)).isNull();
        assertThat(DeedOpsAdapter.canonicalStatus("")).isNull();
        assertThat(DeedOpsAdapter.canonicalStatus("sin firmar")).isEqualTo("Sin Firmar");
        assertThatThrownBy(() -> DeedOpsAdapter.canonicalStatus("BORRADOR"))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    @DisplayName("create defaults blank status to Sin Firmar (CU05)")
    void createDefaultsStatus() {
        when(deedService.save(any(Deed.class))).thenAnswer(invocation -> {
            Deed saved = invocation.getArgument(0);
            saved.setIdDeed(42);
            return saved;
        });

        SaveResult result = adapter.create(new DeedCommand(55, null, null, null, null, null, "seed", null));

        ArgumentCaptor<Deed> captor = ArgumentCaptor.forClass(Deed.class);
        verify(deedService).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo("Sin Firmar");
        assertThat(result).isInstanceOf(SaveResult.Created.class);
        assertThat(((SaveResult.Created) result).view().status()).isEqualTo("Sin Firmar");
    }
}
