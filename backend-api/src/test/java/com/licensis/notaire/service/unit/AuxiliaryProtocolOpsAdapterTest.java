package com.licensis.notaire.service.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.licensis.notaire.adapter.out.persistence.notebook.AuxiliaryProtocolOpsAdapter;
import com.licensis.notaire.application.port.out.notebook.AuxiliaryProtocolOpsPort.StartResult;
import com.licensis.notaire.application.usecase.notebook.AuxiliaryProtocolService;
import com.licensis.notaire.business.Deed;
import com.licensis.notaire.domain.folio.Folio;
import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuxiliaryProtocolOpsAdapter Unit Tests")
class AuxiliaryProtocolOpsAdapterTest {

    @Mock
    private AuxiliaryProtocolService auxiliaryProtocolService;

    private AuxiliaryProtocolOpsAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new AuxiliaryProtocolOpsAdapter(auxiliaryProtocolService);
    }

    @Test
    @DisplayName("Should map a created deed entity to a DeedView")
    void shouldMapCreatedDeedToView() {
        Date when = new Date();
        Deed deed = new Deed();
        deed.setIdDeed(42);
        deed.setNumber(3);
        deed.setBody("aux body");
        deed.setStatus("Draft");
        deed.setDateDeedrecording(when);
        when(auxiliaryProtocolService.startDeed(eq(9), eq("aux body"), any(Date.class))).thenReturn(deed);

        StartResult result = adapter.startDeed(9, "aux body", when);

        assertThat(result).isInstanceOf(StartResult.Created.class);
        assertThat(((StartResult.Created) result).view().idDeed()).isEqualTo(42);
        assertThat(((StartResult.Created) result).view().number()).isEqualTo(3);
        assertThat(((StartResult.Created) result).view().body()).isEqualTo("aux body");
    }

    @Test
    @DisplayName("Should map BusinessValidationException to BadRequest")
    void shouldMapValidationToBadRequest() {
        when(auxiliaryProtocolService.startDeed(eq(1), eq("x"), any(Date.class)))
                .thenThrow(new BusinessValidationException("not auxiliary"));

        StartResult result = adapter.startDeed(1, "x", new Date());

        assertThat(result).isInstanceOf(StartResult.BadRequest.class);
        assertThat(((StartResult.BadRequest) result).message()).isEqualTo("not auxiliary");
    }

    @Test
    @DisplayName("Should map ResourceNotFoundException to NotFound")
    void shouldMapNotFound() {
        when(auxiliaryProtocolService.startDeed(eq(99), eq("x"), any(Date.class)))
                .thenThrow(new ResourceNotFoundException("Folio not found with ID: 99"));

        StartResult result = adapter.startDeed(99, "x", new Date());

        assertThat(result).isInstanceOf(StartResult.NotFound.class);
    }

    @Test
    @DisplayName("Should delegate available folio listing")
    void shouldDelegateListAvailable() {
        Folio folio = Folio.create(5, 2026, "New", null, 1, 1, null);
        when(auxiliaryProtocolService.listAvailableFolios()).thenReturn(List.of(folio));

        assertThat(adapter.listAvailableFolios()).containsExactly(folio);
    }
}
