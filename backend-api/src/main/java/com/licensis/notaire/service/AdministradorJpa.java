package com.licensis.notaire.service;

import com.licensis.notaire.jpa.ConceptJpaController;
import com.licensis.notaire.jpa.CopyJpaController;
import com.licensis.notaire.jpa.SubmittedDocumentJpaController;
import com.licensis.notaire.jpa.DeedJpaController;
import com.licensis.notaire.jpa.ManagementStatusJpaController;
import com.licensis.notaire.jpa.FolioJpaController;
import com.licensis.notaire.jpa.DeedManagementJpaController;
import com.licensis.notaire.jpa.HistoryJpaController;
import com.licensis.notaire.jpa.PropertyJpaController;
import com.licensis.notaire.jpa.ItemJpaController;
import com.licensis.notaire.jpa.TestimonyMovementJpaController;
import com.licensis.notaire.jpa.PaymentJpaController;
import com.licensis.notaire.jpa.PersonJpaController;
import com.licensis.notaire.jpa.BudgetTemplateJpaController;
import com.licensis.notaire.jpa.ProcedureTemplateJpaController;
import com.licensis.notaire.jpa.BudgetJpaController;
import com.licensis.notaire.jpa.AuditRecordJpaController;
import com.licensis.notaire.jpa.SubstitutionJpaController;
import com.licensis.notaire.jpa.TestimonyJpaController;
import com.licensis.notaire.jpa.DocumentTypeJpaController;
import com.licensis.notaire.jpa.FolioTypeJpaController;
import com.licensis.notaire.jpa.ProcedureTypeJpaController;
import com.licensis.notaire.jpa.IdentificationTypeJpaController;
import com.licensis.notaire.jpa.ProcedureJpaController;
import com.licensis.notaire.jpa.PersonProcedureJpaController;
import com.licensis.notaire.jpa.UserJpaController;
import com.licensis.notaire.jpa.exceptions.NonexistentJpaException;
import com.licensis.notaire.jpa.interfaz.IPersistenciaJpa;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import jakarta.persistence.EntityManagerFactory;

public class AdministradorJpa {

    public static final int ERROR = -1;
    private static AdministradorJpa instancia = null;
    private static EntityManagerFactory emf = null;

    private static Collection<IPersistenciaJpa> milistaJpas = null;

    public static void setEmf(EntityManagerFactory factory) {
        emf = factory;
        if (instancia != null) {
            cargarListaJpas();
        }
    }

    private AdministradorJpa() {
        if (emf != null) {
            AdministradorJpa.cargarListaJpas();
        }
    }

    public static AdministradorJpa getInstancia() {
        if (AdministradorJpa.instancia == null) {
            instancia = new AdministradorJpa();
        }
        return instancia;
    }

    public static EntityManagerFactory getEmf() {
        return emf;
    }

    public IPersistenciaJpa obtenerJpa(String nameClase) throws NonexistentJpaException {
        for (Iterator<IPersistenciaJpa> it = milistaJpas.iterator(); it.hasNext();) {
            IPersistenciaJpa iPersistenciaJpa = it.next();
            if (iPersistenciaJpa.getNameJpa().contains(nameClase)) {
                return iPersistenciaJpa;
            }
        }
        throw new NonexistentJpaException("El JPA indicado no existe.");

    }

    public static Collection<IPersistenciaJpa> getMilistaJpas() {
        return milistaJpas;
    }

    public static void setMilistaJpas(Collection<IPersistenciaJpa> milistaJpas) {
        AdministradorJpa.milistaJpas = milistaJpas;
    }

    private static void cargarListaJpas() {
        AdministradorJpa.milistaJpas = new ArrayList<>();
        AdministradorJpa.milistaJpas.add(new ConceptJpaController(null, emf));

        AdministradorJpa.milistaJpas.add(new ManagementStatusJpaController(null, emf));
        AdministradorJpa.milistaJpas.add(new FolioTypeJpaController(null, emf));
        AdministradorJpa.milistaJpas.add(PersonJpaController.getInstancia());
        AdministradorJpa.milistaJpas.add(new SubstitutionJpaController(null, emf));
        AdministradorJpa.milistaJpas.add(UserJpaController.getInstancia());
        AdministradorJpa.milistaJpas.add(new FolioJpaController(null, emf));
        AdministradorJpa.milistaJpas.add(new FolioTypeJpaController(null, emf));
        AdministradorJpa.milistaJpas.add(new DocumentTypeJpaController(null, emf));
        AdministradorJpa.milistaJpas.add(new ProcedureTypeJpaController(null, emf));
        AdministradorJpa.milistaJpas.add(new ProcedureTemplateJpaController(null, emf));
        AdministradorJpa.milistaJpas.add(new IdentificationTypeJpaController(null, emf));
        AdministradorJpa.milistaJpas.add(new HistoryJpaController(null, emf));
        AdministradorJpa.milistaJpas.add(new ProcedureJpaController(null, emf));
        AdministradorJpa.milistaJpas.add(new DeedManagementJpaController(null, emf));
        AdministradorJpa.milistaJpas.add(AuditRecordJpaController.getInstancia());
        AdministradorJpa.milistaJpas.add(new BudgetJpaController(null, emf));
        AdministradorJpa.milistaJpas.add(new ItemJpaController(null, emf));
        AdministradorJpa.milistaJpas.add(new PropertyJpaController(null, emf));
        AdministradorJpa.milistaJpas.add(new BudgetTemplateJpaController(null, emf));
        AdministradorJpa.milistaJpas.add(new PersonProcedureJpaController(emf));
        AdministradorJpa.milistaJpas.add(new PaymentJpaController(null, emf));
        AdministradorJpa.milistaJpas.add(new DeedJpaController(null, emf));
        AdministradorJpa.milistaJpas.add(new CopyJpaController(null, emf));
        AdministradorJpa.milistaJpas.add(new TestimonyJpaController(null, emf));
        AdministradorJpa.milistaJpas.add(new TestimonyMovementJpaController(null, emf));
        AdministradorJpa.milistaJpas.add(new SubmittedDocumentJpaController(null, emf));
    }
}
