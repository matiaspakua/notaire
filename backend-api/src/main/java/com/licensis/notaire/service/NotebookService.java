package com.licensis.notaire.service;

import com.licensis.notaire.exception.BusinessValidationException;
import com.licensis.notaire.exception.ResourceNotFoundException;
import com.licensis.notaire.business.Notebook;
import com.licensis.notaire.business.Folio;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.repository.NotebookRepository;
import com.licensis.notaire.repository.FolioRepository;
import com.licensis.notaire.repository.PersonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class NotebookService {

    private static final int FOLIOS_PER_NOTEBOOK = 10;
    private static final String StatusAssignedToNotebook = "Asignado a cuaderno";
    private static final List<String> DAMAGED_STATUSES = List.of("Errose", "no pasó");

    private static final Logger logger = LoggerFactory.getLogger(NotebookService.class);

    private final NotebookRepository notebookRepository;
    private final FolioRepository folioRepository;
    private final PersonRepository personRepository;

    public NotebookService(NotebookRepository notebookRepository, FolioRepository folioRepository,
                            PersonRepository personRepository) {
        this.notebookRepository = notebookRepository;
        this.folioRepository = folioRepository;
        this.personRepository = personRepository;
    }

    @Transactional(readOnly = true)
    public List<Notebook> findAll() {
        return notebookRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Notebook> findById(Integer id) {
        return notebookRepository.findById(id);
    }

    public Notebook createNotebook(List<Integer> idsFolio, Integer idNotary, int year, String notes) {
        Person notary = personRepository.findById(idNotary)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la persona escribano con ID: " + idNotary));

        List<Folio> folios = folioRepository.findAllByIdFolioIn(idsFolio);
        if (folios.size() != idsFolio.size()) {
            throw new ResourceNotFoundException("Uno o más folios indicados no existen");
        }

        validateFolioCount(folios);
        List<Folio> sortedFolios = folios.stream()
                .sorted(Comparator.comparingInt(Folio::getNumber))
                .toList();
        validateSameNotary(sortedFolios, notary);
        validateConsecutiveness(sortedFolios);
        validateNotAssigned(sortedFolios);
        validateDamagedFolioJustification(sortedFolios, notes);

        Notebook notebook = new Notebook();
        notebook.setYear(year);
        notebook.setNumber(calculateNextNumber(year, notary));
        notebook.setNotes(notes);
        notebook.setFkIdNotaryPerson(notary);
        Notebook savedNotebook = notebookRepository.save(notebook);

        markFoliosAssigned(sortedFolios, savedNotebook);
        logger.info("Cuaderno {}/{} creado para escribano {} con {} folios",
                savedNotebook.getNumber(), savedNotebook.getYear(), notary.getPersonId(), sortedFolios.size());
        return savedNotebook;
    }

    public int calculateNextNumber(int year, Person notary) {
        int candidate = notebookRepository.findByYearAndFkIdNotaryPerson(year, notary).size() + 1;
        while (notebookRepository.existsByNumberAndYearAndFkIdNotaryPerson(candidate, year, notary)) {
            candidate++;
        }
        return candidate;
    }

    public void markFoliosAssigned(List<Folio> folios, Notebook notebook) {
        for (Folio folio : folios) {
            folio.setFkIdNotebook(notebook);
            folio.setStatus(StatusAssignedToNotebook);
        }
        folioRepository.saveAll(folios);
    }

    private void validateFolioCount(List<Folio> folios) {
        if (folios.isEmpty() || folios.size() % FOLIOS_PER_NOTEBOOK != 0) {
            throw new BusinessValidationException(
                    "La cantidad de folios debe ser un múltiplo exacto de " + FOLIOS_PER_NOTEBOOK);
        }
    }

    private void validateSameNotary(List<Folio> folios, Person notary) {
        boolean allSameNotary = folios.stream()
                .allMatch(f -> notary.getPersonId().equals(f.getFkIdNotaryPerson().getPersonId()));
        if (!allSameNotary) {
            throw new BusinessValidationException("Todos los folios deben pertenecer al mismo registro notarial");
        }
    }

    private void validateConsecutiveness(List<Folio> sortedFolios) {
        for (int i = 1; i < sortedFolios.size(); i++) {
            int previous = sortedFolios.get(i - 1).getNumber();
            int current = sortedFolios.get(i).getNumber();
            if (current != previous + 1) {
                throw new BusinessValidationException(
                        "Los folios deben ser estrictamente consecutivos y sin faltantes");
            }
        }
    }

    private void validateNotAssigned(List<Folio> folios) {
        boolean alreadyAssigned = folios.stream().anyMatch(f -> f.getFkIdNotebook() != null);
        if (alreadyAssigned) {
            throw new BusinessValidationException("Uno o más folios ya están asignados a otro cuaderno");
        }
    }

    private void validateDamagedFolioJustification(List<Folio> folios, String notes) {
        boolean hasDamagedFolio = folios.stream().anyMatch(f -> DAMAGED_STATUSES.contains(f.getStatus()));
        if (hasDamagedFolio && (notes == null || notes.isBlank())) {
            throw new BusinessValidationException(
                    "Un folio dañado o anulado en el lote requiere una justificación en observaciones");
        }
    }
}
