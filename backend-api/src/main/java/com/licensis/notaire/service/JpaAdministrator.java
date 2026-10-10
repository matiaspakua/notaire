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
import com.licensis.notaire.jpa.JpaPersistence;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import jakarta.persistence.EntityManagerFactory;

public class JpaAdministrator {

    public static final int ERROR = -1;
    private static JpaAdministrator instancia = null;
    private static EntityManagerFactory emf = null;

    private static Collection<JpaPersistence> milistaJpas = null;

    public static void setEmf(EntityManagerFactory factory) {
        emf = factory;
        if (instancia != null) {
            cargarListaJpas();
        }
    }

    private JpaAdministrator() {
        if (emf != null) {
            JpaAdministrator.cargarListaJpas();
        }
    }

    public static JpaAdministrator getInstancia() {
        if (JpaAdministrator.instancia == null) {
            instancia = new JpaAdministrator();
        }
        return instancia;
    }

    public static EntityManagerFactory getEmf() {
        return emf;
    }

    public JpaPersistence obtenerJpa(String nameClase) throws NonexistentJpaException {
        for (Iterator<JpaPersistence> it = milistaJpas.iterator(); it.hasNext();) {
            JpaPersistence iPersistenciaJpa = it.next();
            if (iPersistenciaJpa.getNameJpa().contains(nameClase)) {
                return iPersistenciaJpa;
            }
        }
        throw new NonexistentJpaException("El JPA indicado no existe.");

    }

    public static Collection<JpaPersistence> getMilistaJpas() {
        return milistaJpas;
    }

    public static void setMilistaJpas(Collection<JpaPersistence> milistaJpas) {
        JpaAdministrator.milistaJpas = milistaJpas;
    }

    private static void cargarListaJpas() {
        JpaAdministrator.milistaJpas = new ArrayList<>();
        JpaAdministrator.milistaJpas.add(new ConceptJpaController(null, emf));

        JpaAdministrator.milistaJpas.add(new ManagementStatusJpaController(null, emf));
        JpaAdministrator.milistaJpas.add(new FolioTypeJpaController(null, emf));
        JpaAdministrator.milistaJpas.add(PersonJpaController.getInstancia());
        JpaAdministrator.milistaJpas.add(new SubstitutionJpaController(null, emf));
        JpaAdministrator.milistaJpas.add(UserJpaController.getInstancia());
        JpaAdministrator.milistaJpas.add(new FolioJpaController(null, emf));
        JpaAdministrator.milistaJpas.add(new FolioTypeJpaController(null, emf));
        JpaAdministrator.milistaJpas.add(new DocumentTypeJpaController(null, emf));
        JpaAdministrator.milistaJpas.add(new ProcedureTypeJpaController(null, emf));
        JpaAdministrator.milistaJpas.add(new ProcedureTemplateJpaController(null, emf));
        JpaAdministrator.milistaJpas.add(new IdentificationTypeJpaController(null, emf));
        JpaAdministrator.milistaJpas.add(new HistoryJpaController(null, emf));
        JpaAdministrator.milistaJpas.add(new ProcedureJpaController(null, emf));
        JpaAdministrator.milistaJpas.add(new DeedManagementJpaController(null, emf));
        JpaAdministrator.milistaJpas.add(AuditRecordJpaController.getInstancia());
        JpaAdministrator.milistaJpas.add(new BudgetJpaController(null, emf));
        JpaAdministrator.milistaJpas.add(new ItemJpaController(null, emf));
        JpaAdministrator.milistaJpas.add(new PropertyJpaController(null, emf));
        JpaAdministrator.milistaJpas.add(new BudgetTemplateJpaController(null, emf));
        JpaAdministrator.milistaJpas.add(new PersonProcedureJpaController(emf));
        JpaAdministrator.milistaJpas.add(new PaymentJpaController(null, emf));
        JpaAdministrator.milistaJpas.add(new DeedJpaController(null, emf));
        JpaAdministrator.milistaJpas.add(new CopyJpaController(null, emf));
        JpaAdministrator.milistaJpas.add(new TestimonyJpaController(null, emf));
        JpaAdministrator.milistaJpas.add(new TestimonyMovementJpaController(null, emf));
        JpaAdministrator.milistaJpas.add(new SubmittedDocumentJpaController(null, emf));
    }
}
