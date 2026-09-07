package com.licensis.notaire.unit;

import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.negocio.Concepto;
import com.licensis.notaire.negocio.Item;
import com.licensis.notaire.negocio.PlantillaPresupuesto;
import com.licensis.notaire.negocio.Presupuesto;
import com.licensis.notaire.repository.ItemRepository;
import com.licensis.notaire.repository.PlantillaPresupuestoRepository;
import com.licensis.notaire.repository.PresupuestoRepository;
import com.licensis.notaire.service.PresupuestoPlantillaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@DisplayName("PresupuestoPlantillaService Tests (CU39)")
@ExtendWith(MockitoExtension.class)
class PresupuestoPlantillaServiceTest {

    @Mock
    private PresupuestoRepository presupuestoRepository;

    @Mock
    private PlantillaPresupuestoRepository plantillaPresupuestoRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private PresupuestoPlantillaService presupuestoPlantillaService;

    private PlantillaPresupuesto buildPlantilla(String nombre, float valor, int porcentaje) {
        Concepto concepto = new Concepto();
        concepto.setNombre(nombre);
        concepto.setValor(valor);
        concepto.setPorcentaje(porcentaje);

        PlantillaPresupuesto plantilla = new PlantillaPresupuesto();
        plantilla.setConcepto(concepto);
        return plantilla;
    }

    @Test
    @DisplayName("Should load one item per concepto of the tipo de trámite's plantilla")
    void shouldLoadItemsFromPlantilla() {
        Presupuesto presupuesto = new Presupuesto();
        presupuesto.setIdPresupuesto(1);
        when(presupuestoRepository.findById(1)).thenReturn(Optional.of(presupuesto));
        when(plantillaPresupuestoRepository.findByTipoDeTramiteIdTipoTramite(5)).thenReturn(List.of(
                buildPlantilla("Honorarios", 1000f, 10),
                buildPlantilla("Sellado", 500f, 0)
        ));
        when(itemRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        List<Item> items = presupuestoPlantillaService.cargarItemsDesdePlantilla(1, 5);

        assertThat(items).hasSize(2);
        assertThat(items).extracting(Item::getNombre).containsExactlyInAnyOrder("Honorarios", "Sellado");
        assertThat(items).extracting(Item::getValor).containsExactlyInAnyOrder(1000f, 500f);
        assertThat(items).allMatch(item -> item.getFkIdPresupuesto() == presupuesto);
    }

    @Test
    @DisplayName("Should not recalculate loaded items when the plantilla changes later")
    void shouldNotRecalculateLoadedItemsWhenPlantillaChanges() {
        Presupuesto presupuesto = new Presupuesto();
        presupuesto.setIdPresupuesto(1);
        PlantillaPresupuesto plantilla = buildPlantilla("Honorarios", 1000f, 10);
        when(presupuestoRepository.findById(1)).thenReturn(Optional.of(presupuesto));
        when(plantillaPresupuestoRepository.findByTipoDeTramiteIdTipoTramite(5))
                .thenReturn(List.of(plantilla));
        when(itemRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        List<Item> items = presupuestoPlantillaService.cargarItemsDesdePlantilla(1, 5);

        // The item copied the concepto's value at load time; changing the plantilla's
        // concepto afterwards must not retroactively change the already-loaded item.
        plantilla.getConcepto().setValor(9999f);

        assertThat(items).extracting(Item::getValor).containsExactly(1000f);
    }

    @Test
    @DisplayName("Should reject loading when the tipo de trámite has no plantilla configured")
    void shouldRejectWhenNoPlantillaForTipoTramite() {
        Presupuesto presupuesto = new Presupuesto();
        presupuesto.setIdPresupuesto(1);
        when(presupuestoRepository.findById(1)).thenReturn(Optional.of(presupuesto));
        when(plantillaPresupuestoRepository.findByTipoDeTramiteIdTipoTramite(99)).thenReturn(List.of());

        assertThatThrownBy(() -> presupuestoPlantillaService.cargarItemsDesdePlantilla(1, 99))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("plantilla");
    }

    @Test
    @DisplayName("Should reject loading when the presupuesto does not exist")
    void shouldRejectWhenPresupuestoDoesNotExist() {
        when(presupuestoRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> presupuestoPlantillaService.cargarItemsDesdePlantilla(999, 5))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
