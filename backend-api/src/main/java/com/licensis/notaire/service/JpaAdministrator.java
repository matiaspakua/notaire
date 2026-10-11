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

    private static Collection<JpaPersistence> jpaList = null;

    public static void setEmf(EntityManagerFactory factory) {
        emf = factory;
        if (instancia != null) {
            loadJpaList();
        }
    }

    private JpaAdministrator() {
        if (emf != null) {
            JpaAdministrator.loadJpaList();
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

    public JpaPersistence getJpa(String className) throws NonexistentJpaException {
        for (Iterator<JpaPersistence> it = jpaList.iterator(); it.hasNext();) {
            JpaPersistence jpaPersistence = it.next();
            if (jpaPersistence.getNameJpa().contains(className)) {
                return jpaPersistence;
            }
        }
        throw new NonexistentJpaException("The indicated JPA does not exist.");

    }

    public static Collection<JpaPersistence> getJpaList() {
        return jpaList;
    }

    public static void setJpaList(Collection<JpaPersistence> jpaList) {
        JpaAdministrator.jpaList = jpaList;
    }

    private static void loadJpaList() {
        JpaAdministrator.jpaList = new ArrayList<>();
        JpaAdministrator.jpaList.add(new ConceptJpaController(null, emf));

        JpaAdministrator.jpaList.add(new ManagementStatusJpaController(null, emf));
        JpaAdministrator.jpaList.add(new FolioTypeJpaController(null, emf));
        JpaAdministrator.jpaList.add(PersonJpaController.getInstancia());
        JpaAdministrator.jpaList.add(new SubstitutionJpaController(null, emf));
        JpaAdministrator.jpaList.add(UserJpaController.getInstancia());
        JpaAdministrator.jpaList.add(new FolioJpaController(null, emf));
        JpaAdministrator.jpaList.add(new FolioTypeJpaController(null, emf));
        JpaAdministrator.jpaList.add(new DocumentTypeJpaController(null, emf));
        JpaAdministrator.jpaList.add(new ProcedureTypeJpaController(null, emf));
        JpaAdministrator.jpaList.add(new ProcedureTemplateJpaController(null, emf));
        JpaAdministrator.jpaList.add(new IdentificationTypeJpaController(null, emf));
        JpaAdministrator.jpaList.add(new HistoryJpaController(null, emf));
        JpaAdministrator.jpaList.add(new ProcedureJpaController(null, emf));
        JpaAdministrator.jpaList.add(new DeedManagementJpaController(null, emf));
        JpaAdministrator.jpaList.add(AuditRecordJpaController.getInstancia());
        JpaAdministrator.jpaList.add(new BudgetJpaController(null, emf));
        JpaAdministrator.jpaList.add(new ItemJpaController(null, emf));
        JpaAdministrator.jpaList.add(new PropertyJpaController(null, emf));
        JpaAdministrator.jpaList.add(new BudgetTemplateJpaController(null, emf));
        JpaAdministrator.jpaList.add(new PersonProcedureJpaController(emf));
        JpaAdministrator.jpaList.add(new PaymentJpaController(null, emf));
        JpaAdministrator.jpaList.add(new DeedJpaController(null, emf));
        JpaAdministrator.jpaList.add(new CopyJpaController(null, emf));
        JpaAdministrator.jpaList.add(new TestimonyJpaController(null, emf));
        JpaAdministrator.jpaList.add(new TestimonyMovementJpaController(null, emf));
        JpaAdministrator.jpaList.add(new SubmittedDocumentJpaController(null, emf));
    }
}
