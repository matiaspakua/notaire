package com.licensis.notaire.service.unit;

import com.licensis.notaire.application.port.in.deed.ValidateDeedNumberingUseCase;
import com.licensis.notaire.domain.deed.DeedNumberingValidationResult;
import com.licensis.notaire.exception.DuplicateDeedNumberException;
import com.licensis.notaire.exception.UnjustifiedNumberingGapException;
import com.licensis.notaire.business.Deed;
import com.licensis.notaire.business.Folio;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.business.FolioType;
import com.licensis.notaire.repository.DeedRepository;
import com.licensis.notaire.repository.FolioRepository;
import com.licensis.notaire.repository.PersonRepository;
import com.licensis.notaire.application.usecase.deed.DeedService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EscrituraService Unit Tests")
class DeedServiceTest {

    @Mock
    private DeedRepository deedRepository;

    @Mock
    private PersonRepository personRepository;

    @Mock
    private FolioRepository folioRepository;

    @Mock
    private ValidateDeedNumberingUseCase validateDeedNumberingUseCase;

    @InjectMocks
    private DeedService deedService;

    private Deed testDeed;
    private Person testNotary;

    @BeforeEach
    void setUp() {
        testNotary = new Person();
        testNotary.setPersonId(1);
        testNotary.setFirstName("Juan");
        testNotary.setLastName("Notary");
        testNotary.setNotaryRegistrationNumber(100);

        testDeed = new Deed();
        testDeed.setIdDeed(1);
        testDeed.setNumber(100);
        testDeed.setBody("Venta de propiedad");
        testDeed.setStatus("FIRMADA");
    }

    @Test
    @DisplayName("Should find all deeds with pagination")
    void shouldFindAllPaged() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Deed> page = new PageImpl<>(List.of(testDeed), pageable, 1);

        when(deedRepository.findAll(pageable)).thenReturn(page);

        Page<Deed> result = deedService.findAllPaged(pageable);

        assertThat(result).isNotNull()
                .hasSize(1)
                .contains(testDeed);
        assertThat(result.getTotalElements()).isEqualTo(1);

        verify(deedRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Should find all deeds")
    void shouldFindAll() {
        List<Deed> escrituras = new ArrayList<>();
        escrituras.add(testDeed);

        when(deedRepository.findAll()).thenReturn(escrituras);

        List<Deed> result = deedService.findAll();

        assertThat(result).isNotNull()
                .hasSize(1)
                .contains(testDeed);

        verify(deedRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return empty list when not deeds exist")
    void shouldReturnEmptyListWhenNoEscriturasExist() {
        when(deedRepository.findAll()).thenReturn(new ArrayList<>());

        List<Deed> result = deedService.findAll();

        assertThat(result).isNotNull()
                .isEmpty();

        verify(deedRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should find deed by id")
    void shouldFindDeedById() {
        when(deedRepository.findById(1)).thenReturn(Optional.of(testDeed));

        Optional<Deed> result = deedService.findById(1);

        assertThat(result).isPresent()
                .contains(testDeed);

        verify(deedRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("Should return empty optional when deed not found")
    void shouldReturnEmptyOptionalWhenDeedNotFound() {
        when(deedRepository.findById(999)).thenReturn(Optional.empty());

        Optional<Deed> result = deedService.findById(999);

        assertThat(result).isEmpty();

        verify(deedRepository, times(1)).findById(999);
    }

    @Test
    @DisplayName("Should save deed")
    void shouldSaveDeed() {
        when(deedRepository.save(testDeed)).thenReturn(testDeed);

        Deed result = deedService.save(testDeed);

        assertThat(result).isNotNull()
                .isEqualTo(testDeed)
                .extracting(Deed::getNumber)
                .isEqualTo(100);

        verify(deedRepository, times(1)).save(testDeed);
    }

    @Test
    @DisplayName("Should reject saving deed when number is a duplicate within its folio scope (CU86)")
    void shouldRejectSaveWhenNumberIsDuplicate() {
        Folio folio = folioConNotary(testNotary, 2026, false);
        testDeed.setIdFolio(10);
        when(folioRepository.findById(10)).thenReturn(Optional.of(folio));
        when(validateDeedNumberingUseCase.validate(100, testNotary, 2026, false, null, 1))
                .thenReturn(DeedNumberingValidationResult.DUPLICATE);

        assertThatThrownBy(() -> deedService.save(testDeed))
                .isInstanceOf(DuplicateDeedNumberException.class);

        verify(deedRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject saving a deed when the number leaves an unjustified gap (CU86)")
    void shouldRejectSaveWhenSaltoIsNotJustified() {
        Folio folio = folioConNotary(testNotary, 2026, false);
        testDeed.setIdFolio(10);
        when(folioRepository.findById(10)).thenReturn(Optional.of(folio));
        when(validateDeedNumberingUseCase.validate(100, testNotary, 2026, false, null, 1))
                .thenReturn(DeedNumberingValidationResult.SKIP_UNJUSTIFIED);

        assertThatThrownBy(() -> deedService.save(testDeed))
                .isInstanceOf(UnjustifiedNumberingGapException.class);

        verify(deedRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should save deed when number correlativo is valid (CU86)")
    void shouldSaveWhenNumberingIsOk() {
        Folio folio = folioConNotary(testNotary, 2026, false);
        testDeed.setIdFolio(10);
        when(folioRepository.findById(10)).thenReturn(Optional.of(folio));
        when(validateDeedNumberingUseCase.validate(100, testNotary, 2026, false, null, 1))
                .thenReturn(DeedNumberingValidationResult.OK);
        when(deedRepository.save(testDeed)).thenReturn(testDeed);

        Deed result = deedService.save(testDeed);

        assertThat(result).isEqualTo(testDeed);
        verify(deedRepository, times(1)).save(testDeed);
    }

    private Folio folioConNotary(Person notary, int year, boolean isAuxiliary) {
        FolioType folioType = new FolioType();
        folioType.setIsAuxiliary(isAuxiliary);

        Folio folio = new Folio();
        folio.setYear(year);
        folio.setFkIdNotaryPerson(notary);
        folio.setFkIdFolioType(folioType);
        return folio;
    }

    @Test
    @DisplayName("Should delete deed by id")
    void shouldDeleteDeedById() {
        deedService.deleteById(1);

        verify(deedRepository, times(1)).deleteById(1);
    }

    @Test
    @DisplayName("Should find all available notaries")
    void shouldFindAvailableNotaries() {
        List<Person> notaries = new ArrayList<>();
        notaries.add(testNotary);

        when(personRepository.findAllNotaries()).thenReturn(notaries);

        List<Person> result = deedService.findAvailableNotaries();

        assertThat(result).isNotNull()
                .hasSize(1)
                .contains(testNotary)
                .allMatch(p -> p.getNotaryRegistrationNumber() != null);

        verify(personRepository, times(1)).findAllNotaries();
    }

    @Test
    @DisplayName("Should return empty list when no notaries available")
    void shouldReturnEmptyListWhenNoNotariesAvailable() {
        when(personRepository.findAllNotaries()).thenReturn(new ArrayList<>());

        List<Person> result = deedService.findAvailableNotaries();

        assertThat(result).isNotNull()
                .isEmpty();

        verify(personRepository, times(1)).findAllNotaries();
    }

    @Test
    @DisplayName("Should search deed by number")
    void shouldSearchDeedByNumber() {
        when(deedRepository.findByNumber(100))
                .thenReturn(Optional.of(testDeed));

        List<Deed> result = deedService.searchByNumber(100);

        assertThat(result).isNotNull()
                .hasSize(1)
                .contains(testDeed);

        verify(deedRepository, times(1)).findByNumber(100);
        verify(deedRepository, never()).findAll();
    }

    @Test
    @DisplayName("Should return empty list when deed number not found")
    void shouldReturnEmptyListWhenDeedNumernoNotFound() {
        when(deedRepository.findByNumber(999))
                .thenReturn(Optional.empty());

        List<Deed> result = deedService.searchByNumber(999);

        assertThat(result).isNotNull()
                .isEmpty();

        verify(deedRepository, times(1)).findByNumber(999);
    }

    @Test
    @DisplayName("Should return all deeds when number is null")
    void shouldReturnAllEscriturasWhenNumberIsNull() {
        List<Deed> escrituras = new ArrayList<>();
        escrituras.add(testDeed);

        when(deedRepository.findAll()).thenReturn(escrituras);

        List<Deed> result = deedService.searchByNumber(null);

        assertThat(result).isNotNull()
                .hasSize(1)
                .contains(testDeed);

        verify(deedRepository, times(1)).findAll();
        verify(deedRepository, never()).findByNumber(anyInt());
    }
}
