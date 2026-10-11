/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.jpa;

import com.licensis.notaire.dto.DtoPerson;
import com.licensis.notaire.jpa.exceptions.ClassDeletedException;
import com.licensis.notaire.jpa.exceptions.ClassModifiedException;
import com.licensis.notaire.jpa.exceptions.IllegalOrphanException;
import com.licensis.notaire.jpa.exceptions.NonexistentEntityException;
import com.licensis.notaire.jpa.JpaPersistence;
import com.licensis.notaire.business.IdentificationTypeLookup;
import com.licensis.notaire.business.Copy;
import com.licensis.notaire.business.Folio;
import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.business.Budget;
import com.licensis.notaire.business.Substitution;
import com.licensis.notaire.business.IdentificationType;
import com.licensis.notaire.business.PersonProcedure;
import com.licensis.notaire.business.User;
import com.licensis.notaire.service.JpaAdministrator;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.UserTransaction;

/**
 *
 * @author matias
 */
public class PersonJpaController implements Serializable, JpaPersistence {

    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;
    private static PersonJpaController instance = null;

    private PersonJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public int create(Person person) {
        int oid = 0;
        if (person.getPersonProcedureList() == null) {
            person.setPersonProcedureList(new ArrayList<PersonProcedure>());
        }
        if (person.getBudgetList() == null) {
            person.setBudgetList(new ArrayList<Budget>());
        }
        if (person.getDeedManagementList() == null) {
            person.setDeedManagementList(new ArrayList<DeedManagement>());
        }
        if (person.getFolioList() == null) {
            person.setFolioList(new ArrayList<Folio>());
        }
        if (person.getSubstitutionList() == null) {
            person.setSubstitutionList(new ArrayList<Substitution>());
        }
        if (person.getSubstitutionList1() == null) {
            person.setSubstitutionList1(new ArrayList<Substitution>());
        }
        if (person.getCopyList() == null) {
            person.setCopyList(new ArrayList<Copy>());
        }
        if (person.getUserList() == null) {
            person.setUserList(new ArrayList<User>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            IdentificationType fkIdIdentificationType = person.getFkIdIdentificationType();
            if (fkIdIdentificationType != null) {
                fkIdIdentificationType = em.getReference(fkIdIdentificationType.getClass(),
                        fkIdIdentificationType.getIdIdentificationType());
                person.setFkIdIdentificationType(fkIdIdentificationType);
            }
            List<PersonProcedure> attachedPersonProcedureList = new ArrayList<PersonProcedure>();
            for (PersonProcedure personProcedureListPersonProcedureToAttach : person.getPersonProcedureList()) {
                personProcedureListPersonProcedureToAttach = em.getReference(
                        personProcedureListPersonProcedureToAttach.getClass(),
                        personProcedureListPersonProcedureToAttach.getPersonProcedurePK());
                attachedPersonProcedureList.add(personProcedureListPersonProcedureToAttach);
            }
            person.setPersonProcedureList(attachedPersonProcedureList);
            List<Budget> attachedBudgetList = new ArrayList<Budget>();
            for (Budget budgetListBudgetToAttach : person.getBudgetList()) {
                budgetListBudgetToAttach = em.getReference(budgetListBudgetToAttach.getClass(),
                        budgetListBudgetToAttach.getIdBudget());
                attachedBudgetList.add(budgetListBudgetToAttach);
            }
            person.setBudgetList(attachedBudgetList);
            List<DeedManagement> attachedDeedManagementsList = new ArrayList<DeedManagement>();
            for (DeedManagement deedManagementListDeedManagementsToAttach : person
                    .getDeedManagementList()) {
                deedManagementListDeedManagementsToAttach = em.getReference(
                        deedManagementListDeedManagementsToAttach.getClass(),
                        deedManagementListDeedManagementsToAttach.getIdManagement());
                attachedDeedManagementsList.add(deedManagementListDeedManagementsToAttach);
            }
            person.setDeedManagementList(attachedDeedManagementsList);
            List<Folio> attachedFolioList = new ArrayList<Folio>();
            for (Folio folioListFolioToAttach : person.getFolioList()) {
                folioListFolioToAttach = em.getReference(folioListFolioToAttach.getClass(),
                        folioListFolioToAttach.getIdFolio());
                attachedFolioList.add(folioListFolioToAttach);
            }
            person.setFolioList(attachedFolioList);
            List<Substitution> attachedSubstitutionList = new ArrayList<Substitution>();
            for (Substitution substitutionListSubstitutionToAttach : person.getSubstitutionList()) {
                substitutionListSubstitutionToAttach = em.getReference(substitutionListSubstitutionToAttach.getClass(),
                        substitutionListSubstitutionToAttach.getIdSubstitution());
                attachedSubstitutionList.add(substitutionListSubstitutionToAttach);
            }
            person.setSubstitutionList(attachedSubstitutionList);
            List<Substitution> attachedSubstitutionList1 = new ArrayList<Substitution>();
            for (Substitution substitutionList1SubstitutionToAttach : person.getSubstitutionList1()) {
                substitutionList1SubstitutionToAttach = em.getReference(substitutionList1SubstitutionToAttach.getClass(),
                        substitutionList1SubstitutionToAttach.getIdSubstitution());
                attachedSubstitutionList1.add(substitutionList1SubstitutionToAttach);
            }
            person.setSubstitutionList1(attachedSubstitutionList1);
            List<Copy> attachedCopyList = new ArrayList<Copy>();
            for (Copy copyListCopyToAttach : person.getCopyList()) {
                copyListCopyToAttach = em.getReference(copyListCopyToAttach.getClass(),
                        copyListCopyToAttach.getIdCopy());
                attachedCopyList.add(copyListCopyToAttach);
            }
            person.setCopyList(attachedCopyList);
            List<User> attachedUserList = new ArrayList<User>();
            for (User userListUserToAttach : person.getUserList()) {
                userListUserToAttach = em.getReference(userListUserToAttach.getClass(),
                        userListUserToAttach.getIdUser());
                attachedUserList.add(userListUserToAttach);
            }
            person.setUserList(attachedUserList);
            em.persist(person);
            if (fkIdIdentificationType != null) {
                fkIdIdentificationType.getPersonList().add(person);
                fkIdIdentificationType = em.merge(fkIdIdentificationType);
            }
            for (PersonProcedure personProcedureListPersonProcedure : person.getPersonProcedureList()) {
                Person oldPersonOfPersonProcedureListPersonProcedure = personProcedureListPersonProcedure
                        .getPerson();
                personProcedureListPersonProcedure.setPerson(person);
                personProcedureListPersonProcedure = em.merge(personProcedureListPersonProcedure);
                if (oldPersonOfPersonProcedureListPersonProcedure != null) {
                    oldPersonOfPersonProcedureListPersonProcedure.getPersonProcedureList()
                            .remove(personProcedureListPersonProcedure);
                    oldPersonOfPersonProcedureListPersonProcedure = em
                            .merge(oldPersonOfPersonProcedureListPersonProcedure);
                }
            }
            for (Budget budgetListBudget : person.getBudgetList()) {
                Person oldFkIdPersonOfBudgetListBudget = budgetListBudget.getFkIdPerson();
                budgetListBudget.setFkIdPerson(person);
                budgetListBudget = em.merge(budgetListBudget);
                if (oldFkIdPersonOfBudgetListBudget != null) {
                    oldFkIdPersonOfBudgetListBudget.getBudgetList().remove(budgetListBudget);
                    oldFkIdPersonOfBudgetListBudget = em.merge(oldFkIdPersonOfBudgetListBudget);
                }
            }
            for (DeedManagement deedManagementListDeedManagements : person
                    .getDeedManagementList()) {
                Person oldFkIdNotaryPersonOfDeedManagementsListDeedManagements = deedManagementListDeedManagements
                        .getFkIdNotaryPerson();
                deedManagementListDeedManagements.setFkIdNotaryPerson(person);
                deedManagementListDeedManagements = em
                        .merge(deedManagementListDeedManagements);
                if (oldFkIdNotaryPersonOfDeedManagementsListDeedManagements != null) {
                    oldFkIdNotaryPersonOfDeedManagementsListDeedManagements.getDeedManagementList()
                            .remove(deedManagementListDeedManagements);
                    oldFkIdNotaryPersonOfDeedManagementsListDeedManagements = em
                            .merge(oldFkIdNotaryPersonOfDeedManagementsListDeedManagements);
                }
            }
            for (Folio folioListFolio : person.getFolioList()) {
                Person oldFkIdNotaryPersonOfFolioListFolio = folioListFolio.getFkIdNotaryPerson();
                folioListFolio.setFkIdNotaryPerson(person);
                folioListFolio = em.merge(folioListFolio);
                if (oldFkIdNotaryPersonOfFolioListFolio != null) {
                    oldFkIdNotaryPersonOfFolioListFolio.getFolioList().remove(folioListFolio);
                    oldFkIdNotaryPersonOfFolioListFolio = em.merge(oldFkIdNotaryPersonOfFolioListFolio);
                }
            }
            for (Substitution substitutionListSubstitution : person.getSubstitutionList()) {
                Person oldFkIdSubstituteOfSubstitutionListSubstitution = substitutionListSubstitution.getFkIdSubstitute();
                substitutionListSubstitution.setFkIdSubstitute(person);
                substitutionListSubstitution = em.merge(substitutionListSubstitution);
                if (oldFkIdSubstituteOfSubstitutionListSubstitution != null) {
                    oldFkIdSubstituteOfSubstitutionListSubstitution.getSubstitutionList().remove(substitutionListSubstitution);
                    oldFkIdSubstituteOfSubstitutionListSubstitution = em.merge(oldFkIdSubstituteOfSubstitutionListSubstitution);
                }
            }
            for (Substitution substitutionList1Substitution : person.getSubstitutionList1()) {
                Person oldFkIdSubstitutedOfSubstitutionList1Substitution = substitutionList1Substitution.getFkIdSubstituted();
                substitutionList1Substitution.setFkIdSubstituted(person);
                substitutionList1Substitution = em.merge(substitutionList1Substitution);
                if (oldFkIdSubstitutedOfSubstitutionList1Substitution != null) {
                    oldFkIdSubstitutedOfSubstitutionList1Substitution.getSubstitutionList1().remove(substitutionList1Substitution);
                    oldFkIdSubstitutedOfSubstitutionList1Substitution = em.merge(oldFkIdSubstitutedOfSubstitutionList1Substitution);
                }
            }
            for (Copy copyListCopy : person.getCopyList()) {
                Person oldFkIdPersonOfCopyListCopy = copyListCopy.getFkIdPerson();
                copyListCopy.setFkIdPerson(person);
                copyListCopy = em.merge(copyListCopy);
                if (oldFkIdPersonOfCopyListCopy != null) {
                    oldFkIdPersonOfCopyListCopy.getCopyList().remove(copyListCopy);
                    oldFkIdPersonOfCopyListCopy = em.merge(oldFkIdPersonOfCopyListCopy);
                }
            }
            for (User userListUser : person.getUserList()) {
                Person oldFkIdPersonOfUserListUser = userListUser.getFkIdPerson();
                userListUser.setFkIdPerson(person);
                userListUser = em.merge(userListUser);
                if (oldFkIdPersonOfUserListUser != null) {
                    oldFkIdPersonOfUserListUser.getUserList().remove(userListUser);
                    oldFkIdPersonOfUserListUser = em.merge(oldFkIdPersonOfUserListUser);
                }
            }
            em.getTransaction().commit();
            oid = person.getPersonId();
        } finally {
            if (em != null) {
                em.close();
            }
        }
        return oid;
    }

    public void edit(Person person) throws IllegalOrphanException, NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Person persistentPerson = em.find(Person.class, person.getPersonId());
            IdentificationType fkIdIdentificationTypeOld = persistentPerson.getFkIdIdentificationType();
            IdentificationType fkIdIdentificationTypeNew = person.getFkIdIdentificationType();
            List<PersonProcedure> personProcedureListOld = persistentPerson.getPersonProcedureList();
            List<PersonProcedure> personProcedureListNew = person.getPersonProcedureList();
            List<Budget> budgetListOld = persistentPerson.getBudgetList();
            List<Budget> budgetListNew = person.getBudgetList();
            List<DeedManagement> deedManagementListOld = persistentPerson.getDeedManagementList();
            List<DeedManagement> deedManagementListNew = person.getDeedManagementList();
            List<Folio> folioListOld = persistentPerson.getFolioList();
            List<Folio> folioListNew = person.getFolioList();
            List<Substitution> substitutionListOld = persistentPerson.getSubstitutionList();
            List<Substitution> substitutionListNew = person.getSubstitutionList();
            List<Substitution> substitutionList1Old = persistentPerson.getSubstitutionList1();
            List<Substitution> substitutionList1New = person.getSubstitutionList1();
            List<Copy> copyListOld = persistentPerson.getCopyList();
            List<Copy> copyListNew = person.getCopyList();
            List<User> userListOld = persistentPerson.getUserList();
            List<User> userListNew = person.getUserList();
            List<String> illegalOrphanMessages = null;
            for (PersonProcedure personProcedureListOldPersonProcedure : personProcedureListOld) {
                if (!personProcedureListNew.contains(personProcedureListOldPersonProcedure)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain PersonProcedure "
                            + personProcedureListOldPersonProcedure + " since its person field is not nullable.");
                }
            }
            for (Budget budgetListOldBudget : budgetListOld) {
                if (!budgetListNew.contains(budgetListOldBudget)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Budget " + budgetListOldBudget
                            + " since its fkIdPerson field is not nullable.");
                }
            }
            for (DeedManagement deedManagementListOldDeedManagements : deedManagementListOld) {
                if (!deedManagementListNew.contains(deedManagementListOldDeedManagements)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add(
                            "You must retain DeedManagements " + deedManagementListOldDeedManagements
                                    + " since its fkIdNotaryPerson field is not nullable.");
                }
            }
            for (Folio folioListOldFolio : folioListOld) {
                if (!folioListNew.contains(folioListOldFolio)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Folio " + folioListOldFolio
                            + " since its fkIdNotaryPerson field is not nullable.");
                }
            }
            for (Substitution substitutionListOldSubstitution : substitutionListOld) {
                if (!substitutionListNew.contains(substitutionListOldSubstitution)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Substitution " + substitutionListOldSubstitution
                            + " since its fkIdSubstitute field is not nullable.");
                }
            }
            for (Substitution substitutionList1OldSubstitution : substitutionList1Old) {
                if (!substitutionList1New.contains(substitutionList1OldSubstitution)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Substitution " + substitutionList1OldSubstitution
                            + " since its fkIdSubstituted field is not nullable.");
                }
            }
            for (Copy copyListOldCopy : copyListOld) {
                if (!copyListNew.contains(copyListOldCopy)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Copy " + copyListOldCopy
                            + " since its fkIdPerson field is not nullable.");
                }
            }
            for (User userListOldUser : userListOld) {
                if (!userListNew.contains(userListOldUser)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain UserAccount " + userListOldUser
                            + " since its fkIdPerson field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (fkIdIdentificationTypeNew != null) {
                fkIdIdentificationTypeNew = em.getReference(fkIdIdentificationTypeNew.getClass(),
                        fkIdIdentificationTypeNew.getIdIdentificationType());
                person.setFkIdIdentificationType(fkIdIdentificationTypeNew);
            }
            List<PersonProcedure> attachedPersonProcedureListNew = new ArrayList<PersonProcedure>();
            for (PersonProcedure personProcedureListNewPersonProcedureToAttach : personProcedureListNew) {
                personProcedureListNewPersonProcedureToAttach = em.getReference(
                        personProcedureListNewPersonProcedureToAttach.getClass(),
                        personProcedureListNewPersonProcedureToAttach.getPersonProcedurePK());
                attachedPersonProcedureListNew.add(personProcedureListNewPersonProcedureToAttach);
            }
            personProcedureListNew = attachedPersonProcedureListNew;
            person.setPersonProcedureList(personProcedureListNew);
            List<Budget> attachedBudgetListNew = new ArrayList<Budget>();
            for (Budget budgetListNewBudgetToAttach : budgetListNew) {
                budgetListNewBudgetToAttach = em.getReference(
                        budgetListNewBudgetToAttach.getClass(),
                        budgetListNewBudgetToAttach.getIdBudget());
                attachedBudgetListNew.add(budgetListNewBudgetToAttach);
            }
            budgetListNew = attachedBudgetListNew;
            person.setBudgetList(budgetListNew);
            List<DeedManagement> attachedDeedManagementsListNew = new ArrayList<DeedManagement>();
            for (DeedManagement deedManagementListNewDeedManagementsToAttach : deedManagementListNew) {
                deedManagementListNewDeedManagementsToAttach = em.getReference(
                        deedManagementListNewDeedManagementsToAttach.getClass(),
                        deedManagementListNewDeedManagementsToAttach.getIdManagement());
                attachedDeedManagementsListNew.add(deedManagementListNewDeedManagementsToAttach);
            }
            deedManagementListNew = attachedDeedManagementsListNew;
            person.setDeedManagementList(deedManagementListNew);
            List<Folio> attachedFolioListNew = new ArrayList<Folio>();
            for (Folio folioListNewFolioToAttach : folioListNew) {
                folioListNewFolioToAttach = em.getReference(folioListNewFolioToAttach.getClass(),
                        folioListNewFolioToAttach.getIdFolio());
                attachedFolioListNew.add(folioListNewFolioToAttach);
            }
            folioListNew = attachedFolioListNew;
            person.setFolioList(folioListNew);
            List<Substitution> attachedSubstitutionListNew = new ArrayList<Substitution>();
            for (Substitution substitutionListNewSubstitutionToAttach : substitutionListNew) {
                substitutionListNewSubstitutionToAttach = em.getReference(substitutionListNewSubstitutionToAttach.getClass(),
                        substitutionListNewSubstitutionToAttach.getIdSubstitution());
                attachedSubstitutionListNew.add(substitutionListNewSubstitutionToAttach);
            }
            substitutionListNew = attachedSubstitutionListNew;
            person.setSubstitutionList(substitutionListNew);
            List<Substitution> attachedSubstitutionList1New = new ArrayList<Substitution>();
            for (Substitution substitutionList1NewSubstitutionToAttach : substitutionList1New) {
                substitutionList1NewSubstitutionToAttach = em.getReference(substitutionList1NewSubstitutionToAttach.getClass(),
                        substitutionList1NewSubstitutionToAttach.getIdSubstitution());
                attachedSubstitutionList1New.add(substitutionList1NewSubstitutionToAttach);
            }
            substitutionList1New = attachedSubstitutionList1New;
            person.setSubstitutionList1(substitutionList1New);
            List<Copy> attachedCopyListNew = new ArrayList<Copy>();
            for (Copy copyListNewCopyToAttach : copyListNew) {
                copyListNewCopyToAttach = em.getReference(copyListNewCopyToAttach.getClass(),
                        copyListNewCopyToAttach.getIdCopy());
                attachedCopyListNew.add(copyListNewCopyToAttach);
            }
            copyListNew = attachedCopyListNew;
            person.setCopyList(copyListNew);
            List<User> attachedUserListNew = new ArrayList<User>();
            for (User userListNewUserToAttach : userListNew) {
                userListNewUserToAttach = em.getReference(userListNewUserToAttach.getClass(),
                        userListNewUserToAttach.getIdUser());
                attachedUserListNew.add(userListNewUserToAttach);
            }
            userListNew = attachedUserListNew;
            person.setUserList(userListNew);
            person = em.merge(person);
            if (fkIdIdentificationTypeOld != null && !fkIdIdentificationTypeOld.equals(fkIdIdentificationTypeNew)) {
                fkIdIdentificationTypeOld.getPersonList().remove(person);
                fkIdIdentificationTypeOld = em.merge(fkIdIdentificationTypeOld);
            }
            if (fkIdIdentificationTypeNew != null && !fkIdIdentificationTypeNew.equals(fkIdIdentificationTypeOld)) {
                fkIdIdentificationTypeNew.getPersonList().add(person);
                fkIdIdentificationTypeNew = em.merge(fkIdIdentificationTypeNew);
            }
            for (PersonProcedure personProcedureListNewPersonProcedure : personProcedureListNew) {
                if (!personProcedureListOld.contains(personProcedureListNewPersonProcedure)) {
                    Person oldPersonOfPersonProcedureListNewPersonProcedure = personProcedureListNewPersonProcedure
                            .getPerson();
                    personProcedureListNewPersonProcedure.setPerson(person);
                    personProcedureListNewPersonProcedure = em.merge(personProcedureListNewPersonProcedure);
                    if (oldPersonOfPersonProcedureListNewPersonProcedure != null
                            && !oldPersonOfPersonProcedureListNewPersonProcedure.equals(person)) {
                        oldPersonOfPersonProcedureListNewPersonProcedure.getPersonProcedureList()
                                .remove(personProcedureListNewPersonProcedure);
                        oldPersonOfPersonProcedureListNewPersonProcedure = em
                                .merge(oldPersonOfPersonProcedureListNewPersonProcedure);
                    }
                }
            }
            for (Budget budgetListNewBudget : budgetListNew) {
                if (!budgetListOld.contains(budgetListNewBudget)) {
                    Person oldFkIdPersonOfBudgetListNewBudget = budgetListNewBudget
                            .getFkIdPerson();
                    budgetListNewBudget.setFkIdPerson(person);
                    budgetListNewBudget = em.merge(budgetListNewBudget);
                    if (oldFkIdPersonOfBudgetListNewBudget != null
                            && !oldFkIdPersonOfBudgetListNewBudget.equals(person)) {
                        oldFkIdPersonOfBudgetListNewBudget.getBudgetList()
                                .remove(budgetListNewBudget);
                        oldFkIdPersonOfBudgetListNewBudget = em
                                .merge(oldFkIdPersonOfBudgetListNewBudget);
                    }
                }
            }
            for (DeedManagement deedManagementListNewDeedManagements : deedManagementListNew) {
                if (!deedManagementListOld.contains(deedManagementListNewDeedManagements)) {
                    Person oldFkIdNotaryPersonOfDeedManagementsListNewDeedManagements = deedManagementListNewDeedManagements
                            .getFkIdNotaryPerson();
                    deedManagementListNewDeedManagements.setFkIdNotaryPerson(person);
                    deedManagementListNewDeedManagements = em
                            .merge(deedManagementListNewDeedManagements);
                    if (oldFkIdNotaryPersonOfDeedManagementsListNewDeedManagements != null
                            && !oldFkIdNotaryPersonOfDeedManagementsListNewDeedManagements
                                    .equals(person)) {
                        oldFkIdNotaryPersonOfDeedManagementsListNewDeedManagements
                                .getDeedManagementList().remove(deedManagementListNewDeedManagements);
                        oldFkIdNotaryPersonOfDeedManagementsListNewDeedManagements = em
                                .merge(oldFkIdNotaryPersonOfDeedManagementsListNewDeedManagements);
                    }
                }
            }
            for (Folio folioListNewFolio : folioListNew) {
                if (!folioListOld.contains(folioListNewFolio)) {
                    Person oldFkIdNotaryPersonOfFolioListNewFolio = folioListNewFolio.getFkIdNotaryPerson();
                    folioListNewFolio.setFkIdNotaryPerson(person);
                    folioListNewFolio = em.merge(folioListNewFolio);
                    if (oldFkIdNotaryPersonOfFolioListNewFolio != null
                            && !oldFkIdNotaryPersonOfFolioListNewFolio.equals(person)) {
                        oldFkIdNotaryPersonOfFolioListNewFolio.getFolioList().remove(folioListNewFolio);
                        oldFkIdNotaryPersonOfFolioListNewFolio = em
                                .merge(oldFkIdNotaryPersonOfFolioListNewFolio);
                    }
                }
            }
            for (Substitution substitutionListNewSubstitution : substitutionListNew) {
                if (!substitutionListOld.contains(substitutionListNewSubstitution)) {
                    Person oldFkIdSubstituteOfSubstitutionListNewSubstitution = substitutionListNewSubstitution.getFkIdSubstitute();
                    substitutionListNewSubstitution.setFkIdSubstitute(person);
                    substitutionListNewSubstitution = em.merge(substitutionListNewSubstitution);
                    if (oldFkIdSubstituteOfSubstitutionListNewSubstitution != null
                            && !oldFkIdSubstituteOfSubstitutionListNewSubstitution.equals(person)) {
                        oldFkIdSubstituteOfSubstitutionListNewSubstitution.getSubstitutionList().remove(substitutionListNewSubstitution);
                        oldFkIdSubstituteOfSubstitutionListNewSubstitution = em
                                .merge(oldFkIdSubstituteOfSubstitutionListNewSubstitution);
                    }
                }
            }
            for (Substitution substitutionList1NewSubstitution : substitutionList1New) {
                if (!substitutionList1Old.contains(substitutionList1NewSubstitution)) {
                    Person oldFkIdSubstitutedOfSubstitutionList1NewSubstitution = substitutionList1NewSubstitution
                            .getFkIdSubstituted();
                    substitutionList1NewSubstitution.setFkIdSubstituted(person);
                    substitutionList1NewSubstitution = em.merge(substitutionList1NewSubstitution);
                    if (oldFkIdSubstitutedOfSubstitutionList1NewSubstitution != null
                            && !oldFkIdSubstitutedOfSubstitutionList1NewSubstitution.equals(person)) {
                        oldFkIdSubstitutedOfSubstitutionList1NewSubstitution.getSubstitutionList1()
                                .remove(substitutionList1NewSubstitution);
                        oldFkIdSubstitutedOfSubstitutionList1NewSubstitution = em
                                .merge(oldFkIdSubstitutedOfSubstitutionList1NewSubstitution);
                    }
                }
            }
            for (Copy copyListNewCopy : copyListNew) {
                if (!copyListOld.contains(copyListNewCopy)) {
                    Person oldFkIdPersonOfCopyListNewCopy = copyListNewCopy.getFkIdPerson();
                    copyListNewCopy.setFkIdPerson(person);
                    copyListNewCopy = em.merge(copyListNewCopy);
                    if (oldFkIdPersonOfCopyListNewCopy != null
                            && !oldFkIdPersonOfCopyListNewCopy.equals(person)) {
                        oldFkIdPersonOfCopyListNewCopy.getCopyList().remove(copyListNewCopy);
                        oldFkIdPersonOfCopyListNewCopy = em.merge(oldFkIdPersonOfCopyListNewCopy);
                    }
                }
            }
            for (User userListNewUser : userListNew) {
                if (!userListOld.contains(userListNewUser)) {
                    Person oldFkIdPersonOfUserListNewUser = userListNewUser.getFkIdPerson();
                    userListNewUser.setFkIdPerson(person);
                    userListNewUser = em.merge(userListNewUser);
                    if (oldFkIdPersonOfUserListNewUser != null
                            && !oldFkIdPersonOfUserListNewUser.equals(person)) {
                        oldFkIdPersonOfUserListNewUser.getUserList().remove(userListNewUser);
                        oldFkIdPersonOfUserListNewUser = em.merge(oldFkIdPersonOfUserListNewUser);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = person.getPersonId();
                if (findPerson(id) == null) {
                    throw new NonexistentEntityException("The person with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Person person;
            try {
                person = em.getReference(Person.class, id);
                person.getPersonId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The person with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<PersonProcedure> personProcedureListOrphanCheck = person.getPersonProcedureList();
            for (PersonProcedure personProcedureListOrphanCheckPersonProcedure : personProcedureListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages
                        .add("This Person (" + person + ") cannot be destroyed since the PersonProcedure "
                                + personProcedureListOrphanCheckPersonProcedure
                                + " in its personProcedureList field has a non-nullable person field.");
            }
            List<Budget> budgetListOrphanCheck = person.getBudgetList();
            for (Budget budgetListOrphanCheckBudget : budgetListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Person (" + person + ") cannot be destroyed since the Budget "
                        + budgetListOrphanCheckBudget
                        + " in its budgetList field has a non-nullable fkIdPerson field.");
            }
            List<DeedManagement> deedManagementListOrphanCheck = person.getDeedManagementList();
            for (DeedManagement deedManagementListOrphanCheckDeedManagements : deedManagementListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Person (" + person
                        + ") cannot be destroyed since the DeedManagements "
                        + deedManagementListOrphanCheckDeedManagements
                        + " in its deedManagementList field has a non-nullable fkIdNotaryPerson field.");
            }
            List<Folio> folioListOrphanCheck = person.getFolioList();
            for (Folio folioListOrphanCheckFolio : folioListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Person (" + person + ") cannot be destroyed since the Folio "
                        + folioListOrphanCheckFolio
                        + " in its folioList field has a non-nullable fkIdNotaryPerson field.");
            }
            List<Substitution> substitutionListOrphanCheck = person.getSubstitutionList();
            for (Substitution substitutionListOrphanCheckSubstitution : substitutionListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Person (" + person + ") cannot be destroyed since the Substitution "
                        + substitutionListOrphanCheckSubstitution
                        + " in its substitutionList field has a non-nullable fkIdSubstitute field.");
            }
            List<Substitution> substitutionList1OrphanCheck = person.getSubstitutionList1();
            for (Substitution substitutionList1OrphanCheckSubstitution : substitutionList1OrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Person (" + person + ") cannot be destroyed since the Substitution "
                        + substitutionList1OrphanCheckSubstitution
                        + " in its substitutionList1 field has a non-nullable fkIdSubstituted field.");
            }
            List<Copy> copyListOrphanCheck = person.getCopyList();
            for (Copy copyListOrphanCheckCopy : copyListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Person (" + person + ") cannot be destroyed since the Copy "
                        + copyListOrphanCheckCopy + " in its copyList field has a non-nullable fkIdPerson field.");
            }
            List<User> userListOrphanCheck = person.getUserList();
            for (User userListOrphanCheckUser : userListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Person (" + person + ") cannot be destroyed since the UserAccount "
                        + userListOrphanCheckUser
                        + " in its userList field has a non-nullable fkIdPerson field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            IdentificationType fkIdIdentificationType = person.getFkIdIdentificationType();
            if (fkIdIdentificationType != null) {
                fkIdIdentificationType.getPersonList().remove(person);
                fkIdIdentificationType = em.merge(fkIdIdentificationType);
            }
            em.remove(person);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Person> findPersonEntities() {
        return findPersonEntities(true, -1, -1);
    }

    public List<Person> findPersonEntities(int maxResults, int firstResult) {
        return findPersonEntities(false, maxResults, firstResult);
    }

    private List<Person> findPersonEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createQuery("select object(o) from Person as o");
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }

            List<Person> people = q.getResultList();

            for (Iterator<Person> it = people.iterator(); it.hasNext();) {
                Person person = it.next();
                person.setFolioList(null);

                person.setDeedManagementList(null);

                person.setBudgetList(null);
                person.setSubstitutionList(null);

                PersonProcedureJpaController jpaProcedurePerson = new PersonProcedureJpaController(emf);
                person.setProcedureList(jpaProcedurePerson.findProceduresPerson(person.getPersonId()));

                person.setPersonProcedureList(null);
                person.setUserList(null);
            }

            return people;
        } finally {
            em.close();
        }
    }

    public Person findPerson(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Person.class, id);
        } finally {
            em.close();
        }
    }

    public int getPersonCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Person> rt = cq.from(Person.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }

    public static PersonJpaController getInstance() {

        EntityManagerFactory emf = JpaAdministrator.getEmf();

        if (instance == null || (instance.emf == null && emf != null)) {
            instance = new PersonJpaController(null, emf);
        }
        return instance;

    }

    public Boolean modifyPerson(Person pPerson) throws ClassModifiedException, ClassDeletedException {

        Boolean flag = false; // Whether the transaction succeeded
        int oldVersion = PersistenceConstants.INITIAL_VERSION; // Version of the object in memory
        int version = PersistenceConstants.INITIAL_VERSION; // Version of the object in the database

        EntityManager em = getEntityManager();

        Person persistentPerson = em.find(Person.class, pPerson.getPersonId());

        if (persistentPerson != null) {
            version = persistentPerson.getVersion(); // Version of the object in the database
            oldVersion = pPerson.getVersion(); // Version of the object in memory

            if (version != oldVersion) // Versions differ: someone else modified the object
            {
                throw new ClassModifiedException("The indicated person has already been modified");
            } else {
                try {

                    em.getTransaction().begin();

                    // Person attributes
                    persistentPerson.setFirstName(pPerson.getFirstName());
                    persistentPerson.setLastName(pPerson.getLastName());
                    persistentPerson.setPhone(pPerson.getPhone());
                    persistentPerson.setEmail(pPerson.getEmail());
                    persistentPerson.setFkIdIdentificationType(pPerson.getFkIdIdentificationType());
                    persistentPerson.setIdentificationNumber(pPerson.getIdentificationNumber());

                    em.getTransaction().commit();
                    em.close();
                } catch (PersistenceException ex) {
                    System.out.println("Persistence error: PersonJpaController method modifyPerson");
                    ex.printStackTrace();
                }
            }
        } else // Already deleted: throw
        {
            throw new ClassDeletedException("The indicated person has been deleted");
        }
        return flag;
    }

    public Boolean registerNotary(Person notary) throws ClassModifiedException, NonexistentEntityException {

        Boolean result = false; // Whether the transaction succeeded
        int oldVersion = PersistenceConstants.INITIAL_VERSION; // Version of the object in memory
        int version = PersistenceConstants.INITIAL_VERSION; // Version of the object in the database

        EntityManager em = getEntityManager();

        Person persistentPerson = em.find(Person.class, notary.getPersonId());

        if (persistentPerson != null) {
            version = persistentPerson.getVersion(); // Version of the object in the database
            oldVersion = notary.getVersion(); // Version of the object in memory

            if (version != oldVersion) // Versions differ: someone else modified the object
            {
                if (em != null) {
                    em.close();
                }
                throw new ClassModifiedException();
            } else {
                em.getTransaction().begin();

                if (notary.getNotaryRegistrationNumber() != 0) {
                    persistentPerson.setNotaryRegistrationNumber(notary.getNotaryRegistrationNumber());
                }
                em.getTransaction().commit();
                result = true;

                if (em != null) {
                    em.close();
                }
            }
        } else // Already deleted: throw
        {
            throw new NonexistentEntityException("The indicated person does not exist");
        }
        return result;
    }

    public Boolean modifyClient(Person pClient) throws ClassModifiedException, ClassDeletedException {

        Boolean flag = false; // Whether the transaction succeeded
        int oldVersion = 0; // Version of the object in memory
        int version = 0; // Version of the object in the database

        EntityManager em = getEntityManager();

        Person persistentPerson = em.find(Person.class, pClient.getPersonId());

        if (persistentPerson != null) {
            version = persistentPerson.getVersion(); // Version of the object in the database
            oldVersion = pClient.getVersion(); // Version of the object in memory

            if (version != oldVersion) // Versions differ: someone else modified the object
            {
                throw new ClassModifiedException("The indicated client has already been modified");

            } else {
                try {
                    em.getTransaction().begin();

                    // Person attributes
                    persistentPerson.setFirstName(pClient.getFirstName());
                    persistentPerson.setLastName(pClient.getLastName());
                    persistentPerson.setPhone(pClient.getPhone());
                    persistentPerson.setEmail(pClient.getEmail());
                    persistentPerson.setFkIdIdentificationType(pClient.getFkIdIdentificationType());
                    persistentPerson.setIdentificationNumber(pClient.getIdentificationNumber());

                    // Hibernate assigns the version.
                    // Client attributes
                    persistentPerson.setNationality(pClient.getNationality());
                    persistentPerson.setBirthDate(pClient.getBirthDate());
                    persistentPerson.setTaxId(pClient.getTaxId());
                    persistentPerson.setMaritalStatus(pClient.getMaritalStatus());
                    persistentPerson.setMarriageCount(pClient.getMarriageCount());
                    persistentPerson.setSex(pClient.getSex());
                    persistentPerson.setOccupation(pClient.getOccupation());
                    persistentPerson.setAddress(pClient.getAddress());

                    persistentPerson.setIsClient(pClient.getIsClient());

                    persistentPerson.setNotaryRegistrationNumber(pClient.getNotaryRegistrationNumber());

                    em.getTransaction().commit();
                    em.close();
                    flag = true;
                } catch (Exception e) {
                    System.out.println("Persistence error: PersonJpaController method modifyClient");
                }
            }
        } else // Already deleted: throw
        {
            throw new ClassDeletedException("The indicated client has been deleted or does not exist");
        }
        return flag;
    }

    /**
     * Looks up a person by identification type and number.
     *
     * @param dtoPerson identification type and number to match
     * @return the matching person, or null when none exists
     */
    public Person findPersonTypeIdentificationNumber(DtoPerson dtoPerson) {
        EntityManager em = getEntityManager();
        List<Person> people = null;
        Person person = null;
        // Resolve the identification type name to its id before the lookup.
        dtoPerson.getDtoIdentificationType().setIdIdentificationType(
                IdentificationTypeLookup.shared().idOf(dtoPerson.getDtoIdentificationType().getName()));

        String identificationNumber = dtoPerson.getIdentificationNumber();
        int idIdentificationType = dtoPerson.getDtoIdentificationType().getIdIdentificationType();

        Query query = em.createNamedQuery("Person.findByIdentificationNumber");
        query.setParameter("identificationNumber", identificationNumber);

        people = query.getResultList();

        if (!people.isEmpty()) {
            for (int i = 0; i < people.size(); i++) {
                if (people.get(i).getFkIdIdentificationType().getIdIdentificationType() == idIdentificationType) {
                    person = people.get(i);
                }

            }
        }

        return person;
    }

    /**
     * Looks up people by an approximate first name and last name.
     *
     * @param dtoPerson name fragment to match
     * @return people whose names contain the given fragments
     */
    public List<Person> findPersonNameLastName(DtoPerson dtoPerson) {

        EntityManager em = getEntityManager();

        List<Person> people = null;
        Person person = null;
        String name = "%" + dtoPerson.getFirstName() + "%";
        String lastName = "%" + dtoPerson.getLastName() + "%";

        Query query = em.createNamedQuery("Person.findByPersonNameLastName");
        query.setParameter("name", name);
        query.setParameter("lastName", lastName);

        people = query.getResultList();

        return people;
    }

    /**
     * Returns every person.
     *
     * @return all people
     */
    public List<Person> findPersons() {
        EntityManager em = getEntityManager();

        List<Person> people = null;
        Person person = null;
        try {
            Query query = em.createNamedQuery("Person.findAll");

            people = query.getResultList();
        } catch (PersistenceException ex) {
            ex.printStackTrace();
        }
        return people;
    }

    /**
     * Looks up a notary by registration number.
     *
     * @param notary person carrying the notary registration number
     * @return the matching notary, or null when none exists
     */
    public Person findPersonNotary(Person notary) {

        EntityManager em = getEntityManager();

        List<Person> people = null;
        Person person = null;

        Query query = em.createNamedQuery("Person.findByNotaryRegistrationNumber");
        query.setParameter("notaryRegistrationNumber", notary.getNotaryRegistrationNumber());

        people = query.getResultList();

        if (people != null) {
            person = people.get(0);
        }

        return person;
    }

    public Person findPersonById(Integer idPerson) {
        EntityManager em = getEntityManager();

        Person person = new Person();

        Query query = em.createNamedQuery("Person.findByIdPerson");
        query.setParameter("idPerson", idPerson);

        person = (Person) query.getSingleResult();

        return person;
    }

    @Override
    public String getNameJpa() {
        return this.getClass().getName();
    }
}
