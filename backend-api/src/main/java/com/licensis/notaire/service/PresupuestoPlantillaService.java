package com.licensis.notaire.service;

import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.negocio.Concepto;
import com.licensis.notaire.negocio.Item;
import com.licensis.notaire.negocio.PlantillaPresupuesto;
import com.licensis.notaire.negocio.Presupuesto;
import com.licensis.notaire.repository.ItemRepository;
import com.licensis.notaire.repository.PlantillaPresupuestoRepository;
import com.licensis.notaire.repository.PresupuestoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Loads the Conceptos of a TipoDeTramite's PlantillaPresupuesto as Items of a
 * real Presupuesto (CU39).
 */
@Service
@Transactional
public class PresupuestoPlantillaService {

    private final PresupuestoRepository presupuestoRepository;
    private final PlantillaPresupuestoRepository plantillaPresupuestoRepository;
    private final ItemRepository itemRepository;

    public PresupuestoPlantillaService(PresupuestoRepository presupuestoRepository,
            PlantillaPresupuestoRepository plantillaPresupuestoRepository,
            ItemRepository itemRepository) {
        this.presupuestoRepository = presupuestoRepository;
        this.plantillaPresupuestoRepository = plantillaPresupuestoRepository;
        this.itemRepository = itemRepository;
    }

    public List<Item> cargarItemsDesdePlantilla(Integer idPresupuesto, Integer tipoTramiteId) {
        Presupuesto presupuesto = presupuestoRepository.findById(idPresupuesto)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Presupuesto no encontrado con id: " + idPresupuesto));

        List<PlantillaPresupuesto> plantillas =
                plantillaPresupuestoRepository.findByTipoDeTramiteIdTipoTramite(tipoTramiteId);

        if (plantillas.isEmpty()) {
            throw new BusinessValidationException(
                    "El tipo de trámite no tiene plantilla configurada");
        }

        List<Item> items = plantillas.stream()
                .map(plantilla -> crearItemDesdeConcepto(presupuesto, plantilla.getConcepto()))
                .toList();

        return itemRepository.saveAll(items);
    }

    private Item crearItemDesdeConcepto(Presupuesto presupuesto, Concepto concepto) {
        Item item = new Item();
        item.setNombre(concepto.getNombre());
        item.setValor(concepto.getValor());
        item.setPorcentaje(concepto.getPorcentaje());
        item.setFkIdPresupuesto(presupuesto);
        return item;
    }
}
