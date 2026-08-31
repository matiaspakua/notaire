package com.licensis.notaire.integration;

import com.licensis.notaire.negocio.Inmueble;
import com.licensis.notaire.repository.InmuebleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Inmueble Repository Integration Tests")
class InmuebleRepositoryIntegrationTest extends ServiceIntegrationTest {

    @Autowired
    private InmuebleRepository inmuebleRepository;

    @BeforeEach
    void setUp() {
        inmuebleRepository.deleteAll();
    }

    @Test
    @DisplayName("Should persist inmueble with all fields")
    void shouldPersistInmuebleWithAllFields() {
        Inmueble inmueble = new Inmueble();
        inmueble.setNomenclaturaCatastral("Nomenclatura 123");
        inmueble.setDomicilio("Calle Principal 123, Apartamento 4B");
        inmueble.setValuacionFiscal(100000f);
        inmueble.setObservaciones("Test inmueble");
        Inmueble saved = inmuebleRepository.save(inmueble);

        assertThat(saved.getIdInmueble()).isNotNull();
        assertThat(saved.getDomicilio()).isEqualTo("Calle Principal 123, Apartamento 4B");
        assertThat(saved.getNomenclaturaCatastral()).isEqualTo("Nomenclatura 123");
        assertThat(saved.getValuacionFiscal()).isEqualTo(100000f);
        assertThat(saved.getObservaciones()).isEqualTo("Test inmueble");
    }

    @Test
    @DisplayName("Should retrieve inmueble by ID")
    void shouldRetrieveInmuebleById() {
        Inmueble inmueble = new Inmueble();
        inmueble.setNomenclaturaCatastral("Test 456");
        inmueble.setDomicilio("Calle Principal 123, Apartamento 4B");
        Inmueble saved = inmuebleRepository.save(inmueble);

        Optional<Inmueble> found = inmuebleRepository.findById(saved.getIdInmueble());

        assertThat(found).isPresent();
        assertThat(found.get().getDomicilio()).isEqualTo("Calle Principal 123, Apartamento 4B");
    }

    @Test
    @DisplayName("Should find all inmuebles")
    void shouldFindAllInmuebles() {
        Inmueble inmueble = new Inmueble();
        inmueble.setNomenclaturaCatastral("Test 789");
        inmueble.setDomicilio("Calle Principal 123, Apartamento 4B");
        inmuebleRepository.save(inmueble);

        List<Inmueble> all = inmuebleRepository.findAll();

        assertThat(all).isNotEmpty();
        assertThat(all).anyMatch(i -> i.getDomicilio().equals("Calle Principal 123, Apartamento 4B"));
    }

    @Test
    @DisplayName("Should update inmueble")
    void shouldUpdateInmueble() {
        Inmueble inmueble = new Inmueble();
        inmueble.setNomenclaturaCatastral("Update Test");
        inmueble.setDomicilio("Original Street");
        inmueble.setValuacionFiscal(100000f);
        Inmueble saved = inmuebleRepository.save(inmueble);

        saved.setDomicilio("Avenida Corrientes 1234");
        saved.setValuacionFiscal(150000f);
        Inmueble updated = inmuebleRepository.save(saved);

        assertThat(updated.getDomicilio()).isEqualTo("Avenida Corrientes 1234");
        assertThat(updated.getValuacionFiscal()).isEqualTo(150000f);

        Optional<Inmueble> fetched = inmuebleRepository.findById(saved.getIdInmueble());
        assertThat(fetched.get().getDomicilio()).isEqualTo("Avenida Corrientes 1234");
    }

    @Test
    @DisplayName("Should delete inmueble")
    void shouldDeleteInmueble() {
        Inmueble inmueble = new Inmueble();
        inmueble.setNomenclaturaCatastral("Delete Test");
        inmueble.setDomicilio("To Be Deleted");
        Inmueble saved = inmuebleRepository.save(inmueble);
        Integer id = saved.getIdInmueble();

        inmuebleRepository.delete(saved);

        Optional<Inmueble> deleted = inmuebleRepository.findById(id);
        assertThat(deleted).isEmpty();
    }

    @Test
    @DisplayName("Should handle nullable fields")
    void shouldHandleNullableFields() {
        Inmueble inmueble = new Inmueble();
        inmueble.setNomenclaturaCatastral("Test 456");
        inmueble.setDomicilio("Dirección Mínima");
        inmueble.setValuacionFiscal(null);
        inmueble.setObservaciones(null);

        Inmueble saved = inmuebleRepository.save(inmueble);

        assertThat(saved.getValuacionFiscal()).isNull();
        assertThat(saved.getObservaciones()).isNull();
    }

    @Test
    @DisplayName("Should support multiple inmuebles")
    void shouldSupportMultipleInmuebles() {
        for (int i = 0; i < 5; i++) {
            Inmueble inmueble = new Inmueble();
            inmueble.setNomenclaturaCatastral("Nomenclatura " + i);
            inmueble.setDomicilio("Calle " + i);
            inmueble.setValuacionFiscal((float) (50000 + (i * 10000)));
            inmuebleRepository.save(inmueble);
        }

        List<Inmueble> all = inmuebleRepository.findAll();
        assertThat(all.size()).isEqualTo(5);
    }

    @Test
    @DisplayName("Should handle long address strings")
    void shouldHandleLongAddressStrings() {
        Inmueble inmueble = new Inmueble();
        String longAddress = "Calle " + "X".repeat(100) + " Número 9999, Apartamento 10Z, Piso 50";
        inmueble.setNomenclaturaCatastral("Long Address Test");
        inmueble.setDomicilio(longAddress);

        Inmueble saved = inmuebleRepository.save(inmueble);

        assertThat(saved.getDomicilio()).startsWith("Calle X");
        assertThat(saved.getDomicilio()).contains("Número 9999");
    }
}
