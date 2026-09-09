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

    private static final int FOLIOSPORNotebook = 10;
    private static final String StatusASIGNADOANotebook = "Asignado a cuaderno";
    private static final List<String> ESTADOS_DANADOS = List.of("Errose", "no pasó");

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

    public Notebook crearNotebook(List<Integer> idsFolio, Integer idNotary, int year, String notes) {
        Person notary = personRepository.findById(idNotary)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la persona escribano con ID: " + idNotary));

        List<Folio> folios = folioRepository.findAllByIdFolioIn(idsFolio);
        if (folios.size() != idsFolio.size()) {
            throw new ResourceNotFoundException("Uno o más folios indicados no existen");
        }

        validateFolioCount(folios);
        List<Folio> foliosOrdenados = folios.stream()
                .sorted(Comparator.comparingInt(Folio::getNumber))
                .toList();
        validarMismoNotary(foliosOrdenados, notary);
        validarConsecutividad(foliosOrdenados);
        validarNoAsignados(foliosOrdenados);
        validateDamagedFolioJustification(foliosOrdenados, notes);

        Notebook notebook = new Notebook();
        notebook.setYear(year);
        notebook.setNumber(calcularSiguienteNumber(year, notary));
        notebook.setNotes(notes);
        notebook.setFkIdNotaryPerson(notary);
        Notebook guardado = notebookRepository.save(notebook);

        markFoliosAssigned(foliosOrdenados, guardado);
        logger.info("Cuaderno {}/{} creado para escribano {} con {} folios",
                guardado.getNumber(), guardado.getYear(), notary.getPersonId(), foliosOrdenados.size());
        return guardado;
    }

    public int calcularSiguienteNumber(int year, Person notary) {
        int candidato = notebookRepository.findByYearAndFkIdNotaryPerson(year, notary).size() + 1;
        while (notebookRepository.existsByNumberAndYearAndFkIdNotaryPerson(candidato, year, notary)) {
            candidato++;
        }
        return candidato;
    }

    public void markFoliosAssigned(List<Folio> folios, Notebook notebook) {
        for (Folio folio : folios) {
            folio.setFkIdNotebook(notebook);
            folio.setStatus(StatusASIGNADOANotebook);
        }
        folioRepository.saveAll(folios);
    }

    private void validateFolioCount(List<Folio> folios) {
        if (folios.isEmpty() || folios.size() % FOLIOSPORNotebook != 0) {
            throw new BusinessValidationException(
                    "La cantidad de folios debe ser un múltiplo exacto de " + FOLIOSPORNotebook);
        }
    }

    private void validarMismoNotary(List<Folio> folios, Person notary) {
        boolean todosMismoNotary = folios.stream()
                .allMatch(f -> notary.getPersonId().equals(f.getFkIdNotaryPerson().getPersonId()));
        if (!todosMismoNotary) {
            throw new BusinessValidationException("Todos los folios deben pertenecer al mismo registro notarial");
        }
    }

    private void validarConsecutividad(List<Folio> foliosOrdenados) {
        for (int i = 1; i < foliosOrdenados.size(); i++) {
            int anterior = foliosOrdenados.get(i - 1).getNumber();
            int actual = foliosOrdenados.get(i).getNumber();
            if (actual != anterior + 1) {
                throw new BusinessValidationException(
                        "Los folios deben ser estrictamente consecutivos y sin faltantes");
            }
        }
    }

    private void validarNoAsignados(List<Folio> folios) {
        boolean yaAsignado = folios.stream().anyMatch(f -> f.getFkIdNotebook() != null);
        if (yaAsignado) {
            throw new BusinessValidationException("Uno o más folios ya están asignados a otro cuaderno");
        }
    }

    private void validateDamagedFolioJustification(List<Folio> folios, String notes) {
        boolean hayFolioDanado = folios.stream().anyMatch(f -> ESTADOS_DANADOS.contains(f.getStatus()));
        if (hayFolioDanado && (notes == null || notes.isBlank())) {
            throw new BusinessValidationException(
                    "Un folio dañado o anulado en el lote requiere una justificación en observaciones");
        }
    }
}
