/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.jpa;

import com.licensis.notaire.jpa.exceptions.IllegalOrphanException;
import com.licensis.notaire.jpa.exceptions.NonexistentEntityException;
import com.licensis.notaire.jpa.interfaz.IPersistenciaJpa;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.business.IdentificationType;
import com.licensis.notaire.service.AdministradorJpa;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.Query;
import jakarta.transaction.UserTransaction;

/**
 *
 * @author juanca
 */
public class IdentificationTypeJpaController implements Serializable, IPersistenciaJpa {

    private static IdentificationTypeJpaController instancia = null;

    public IdentificationTypeJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }

    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(IdentificationType identificationType) {
        if (identificationType.getPersonList() == null) {
            identificationType.setPersonList(new ArrayList<Person>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            List<Person> attachedPersonList = new ArrayList<Person>();
            for (Person personListPersonToAttach : identificationType.getPersonList()) {
                personListPersonToAttach = em.getReference(personListPersonToAttach.getClass(),
                        personListPersonToAttach.getPersonId());
                attachedPersonList.add(personListPersonToAttach);
            }
            identificationType.setPersonList(attachedPersonList);
            em.persist(identificationType);
            for (Person personListPerson : identificationType.getPersonList()) {
                IdentificationType oldFkIdIdentificationTypeOfPersonListPerson = personListPerson
                        .getFkIdIdentificationType();
                personListPerson.setFkIdIdentificationType(identificationType);
                personListPerson = em.merge(personListPerson);
                if (oldFkIdIdentificationTypeOfPersonListPerson != null) {
                    oldFkIdIdentificationTypeOfPersonListPerson.getPersonList().remove(personListPerson);
                    oldFkIdIdentificationTypeOfPersonListPerson = em
                            .merge(oldFkIdIdentificationTypeOfPersonListPerson);
                }
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(IdentificationType identificationType)
            throws IllegalOrphanException, NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            IdentificationType persistentIdentificationType = em.find(IdentificationType.class,
                    identificationType.getIdIdentificationType());
            List<Person> personListOld = persistentIdentificationType.getPersonList();
            List<Person> personListNew = identificationType.getPersonList();
            List<String> illegalOrphanMessages = null;
            for (Person personListOldPerson : personListOld) {
                if (!personListNew.contains(personListOldPerson)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Persona " + personListOldPerson
                            + " since its fkIdTipoIdentificacion field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            List<Person> attachedPersonListNew = new ArrayList<Person>();
            for (Person personListNewPersonToAttach : personListNew) {
                personListNewPersonToAttach = em.getReference(personListNewPersonToAttach.getClass(),
                        personListNewPersonToAttach.getPersonId());
                attachedPersonListNew.add(personListNewPersonToAttach);
            }
            personListNew = attachedPersonListNew;
            identificationType.setPersonList(personListNew);
            identificationType = em.merge(identificationType);
            for (Person personListNewPerson : personListNew) {
                if (!personListOld.contains(personListNewPerson)) {
                    IdentificationType oldFkIdIdentificationTypeOfPersonListNewPerson = personListNewPerson
                            .getFkIdIdentificationType();
                    personListNewPerson.setFkIdIdentificationType(identificationType);
                    personListNewPerson = em.merge(personListNewPerson);
                    if (oldFkIdIdentificationTypeOfPersonListNewPerson != null
                            && !oldFkIdIdentificationTypeOfPersonListNewPerson.equals(identificationType)) {
                        oldFkIdIdentificationTypeOfPersonListNewPerson.getPersonList().remove(personListNewPerson);
                        oldFkIdIdentificationTypeOfPersonListNewPerson = em
                                .merge(oldFkIdIdentificationTypeOfPersonListNewPerson);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = identificationType.getIdIdentificationType();
                if (findIdentificationType(id) == null) {
                    throw new NonexistentEntityException("The tipoIdentificacion with id " + id + " no longer exists.");
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
            IdentificationType identificationType;
            try {
                identificationType = em.getReference(IdentificationType.class, id);
                identificationType.getIdIdentificationType();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The tipoIdentificacion with id " + id + " no longer exists.",
                        enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<Person> personListOrphanCheck = identificationType.getPersonList();
            for (Person personListOrphanCheckPerson : personListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This TipoIdentificacion (" + identificationType
                        + ") cannot be destroyed since the Persona " + personListOrphanCheckPerson
                        + " in its personaList field has a non-nullable fkIdTipoIdentificacion field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(identificationType);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<IdentificationType> findIdentificationTypeEntities() {
        return findIdentificationTypeEntities(true, -1, -1);
    }

    public List<IdentificationType> findIdentificationTypeEntities(int maxResults, int firstResult) {
        return findIdentificationTypeEntities(false, maxResults, firstResult);
    }

    private List<IdentificationType> findIdentificationTypeEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createQuery("select object(o) from TipoIdentificacion as o");
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public IdentificationType findIdentificationType(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(IdentificationType.class, id);
        } finally {
            em.close();
        }
    }

    public int getIdentificationTypeCount() {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createQuery("select count(o) from TipoIdentificacion as o");
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }

    public static IdentificationTypeJpaController getInstancia() {

        EntityManagerFactory emf = AdministradorJpa.getEmf();

        if (instancia == null || (instancia.emf == null && emf != null)) {
            instancia = new IdentificationTypeJpaController(null, emf);
        }
        return instancia;
    }

    @Override
    public String getNameJpa() {
        return this.getClass().getName();
    }
}
