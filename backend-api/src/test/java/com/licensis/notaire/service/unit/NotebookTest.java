package com.licensis.notaire.service.unit;

import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.business.Notebook;
import com.licensis.notaire.business.Folio;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.repository.NotebookRepository;
import com.licensis.notaire.repository.FolioRepository;
import com.licensis.notaire.repository.PersonRepository;
import com.licensis.notaire.service.NotebookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CuadernoService Unit Tests")
class NotebookTest {

    @Mock
    private NotebookRepository notebookRepository;

    @Mock
    private FolioRepository folioRepository;

    @Mock
    private PersonRepository personRepository;

    @InjectMocks
    private NotebookService notebookService;

    private Person notary;

    @BeforeEach
    void setUp() {
        notary = new Person();
        notary.setPersonId(1);
        notary.setNotaryRegistrationNumber(7);
    }

    private Folio folio(Integer id, int number, String status) {
        Folio folio = new Folio();
        folio.setIdFolio(id);
        folio.setNumber(number);
        folio.setYear(2026);
        folio.setStatus(status);
        folio.setFkIdNotaryPerson(notary);
        return folio;
    }

    @Test
    @DisplayName("Should mark all folios as Asignado a cuaderno")
    void shouldMarkFoliosAsAsignadoANotebook() {
        List<Folio> folios = new ArrayList<>(List.of(folio(1, 1, "Nuevo"), folio(2, 2, "Nuevo")));
        Notebook notebook = new Notebook();
        notebook.setIdNotebook(10);

        notebookService.marcarFoliosAsignados(folios, notebook);

        assertThat(folios).allSatisfy(f -> {
            assertThat(f.getStatus()).isEqualTo("Asignado a cuaderno");
            assertThat(f.getFkIdNotebook()).isEqualTo(notebook);
        });
    }

    @Test
    @DisplayName("Should assign number one to the first cuaderno of the year for a registro")
    void shouldAssignNumberOneToFirstNotebookOfYear() {
        when(notebookRepository.findByYearAndFkIdNotaryPerson(2026, notary)).thenReturn(List.of());
        when(notebookRepository.existsByNumberAndYearAndFkIdNotaryPerson(1, 2026, notary)).thenReturn(false);

        int number = notebookService.calcularSiguienteNumber(2026, notary);

        assertThat(number).isEqualTo(1);
    }

    @Test
    @DisplayName("Should recalculate the next available cuaderno number on conflict")
    void shouldRecalculateNextAvailableNotebookNumber() {
        when(notebookRepository.findByYearAndFkIdNotaryPerson(2026, notary))
                .thenReturn(List.of(new Notebook()));
        when(notebookRepository.existsByNumberAndYearAndFkIdNotaryPerson(2, 2026, notary)).thenReturn(true);
        when(notebookRepository.existsByNumberAndYearAndFkIdNotaryPerson(3, 2026, notary)).thenReturn(false);

        int number = notebookService.calcularSiguienteNumber(2026, notary);

        assertThat(number).isEqualTo(3);
    }

    @Test
    @DisplayName("Should reject cuaderno creation when notary does not exist")
    void shouldRejectCreationWhenNotaryNotFound() {
        when(personRepository.findById(anyInt())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notebookService.crearNotebook(List.of(1, 2), 99, 2026, null))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should reject cuaderno creation when folio count is not a multiple of ten")
    void shouldRejectWhenFolioCountNotMultipleOfTen() {
        when(personRepository.findById(1)).thenReturn(Optional.of(notary));
        List<Folio> folios = List.of(folio(1, 1, "Nuevo"), folio(2, 2, "Nuevo"));
        when(folioRepository.findAllByIdFolioIn(List.of(1, 2))).thenReturn(folios);

        assertThatThrownBy(() -> notebookService.crearNotebook(List.of(1, 2), 1, 2026, null))
                .isInstanceOf(BusinessValidationException.class);
    }
}
