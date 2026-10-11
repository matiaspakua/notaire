/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.jpa;

import com.licensis.notaire.jpa.exceptions.ClassEliminatedException;
import com.licensis.notaire.jpa.exceptions.ClassModifiedException;
import com.licensis.notaire.jpa.exceptions.IllegalOrphanException;
import com.licensis.notaire.jpa.exceptions.NonexistentEntityException;
import com.licensis.notaire.jpa.exceptions.PreexistingEntityException;
import com.licensis.notaire.jpa.JpaPersistence;
import com.licensis.notaire.business.ManagementStatus;
import com.licensis.notaire.business.History;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.Query;
import jakarta.transaction.UserTransaction;

/**
 *
 * @author User
 */
public class ManagementStatusJpaController implements Serializable, JpaPersistence {

    public ManagementStatusJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }

    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public Integer create(ManagementStatus managementStatus) throws PreexistingEntityException {
        Integer oid = null;
        if (managementStatus.getHistoryList() == null) {
            managementStatus.setHistoryList(new java.util.HashSet<History>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            java.util.Set<History> attachedHistoryList = new java.util.HashSet<History>();
            for (History historyListHistoryToAttach : managementStatus.getHistoryList()) {
                historyListHistoryToAttach = em.getReference(historyListHistoryToAttach.getClass(),
                        historyListHistoryToAttach.getIdHistory());
                attachedHistoryList.add(historyListHistoryToAttach);
            }
            managementStatus.setHistoryList(attachedHistoryList);
            em.persist(managementStatus);
            for (History historyListHistory : managementStatus.getHistoryList()) {
                ManagementStatus oldFkIdManagementStatusOfHistoryListHistory = historyListHistory
                        .getFkIdManagementStatus();
                historyListHistory.setFkIdManagementStatus(managementStatus);
                historyListHistory = em.merge(historyListHistory);
                if (oldFkIdManagementStatusOfHistoryListHistory != null) {
                    oldFkIdManagementStatusOfHistoryListHistory.getHistoryList().remove(historyListHistory);
                    oldFkIdManagementStatusOfHistoryListHistory = em
                            .merge(oldFkIdManagementStatusOfHistoryListHistory);
                }
            }

            if (this.verifyManagementStatusExists(managementStatus.getName())) {
                throw new PreexistingEntityException("The entity already exists");
            }

            em.getTransaction().commit();

            oid = managementStatus.getIdManagementStatus();
        } finally {
            if (em != null) {
                em.close();
            }
        }

        return oid;
    }

    public Boolean edit(ManagementStatus managementStatus) throws IllegalOrphanException, NonexistentEntityException,
            ClassEliminatedException, ClassModifiedException {
        Integer version = PersistenceConstants.INITIAL_VERSION;
        Integer oldVersion = PersistenceConstants.INITIAL_VERSION;
        Boolean result = Boolean.FALSE;
        EntityManager em = null;

        em = getEntityManager();
        em.getTransaction().begin();
        ManagementStatus persistentManagementStatus = em.find(ManagementStatus.class,
                managementStatus.getIdManagementStatus());

        if (persistentManagementStatus != null) {
            version = persistentManagementStatus.getVersion();
            oldVersion = managementStatus.getVersion();

            if (version != oldVersion) // Versions differ: someone else modified or deleted the object
            {
                if (em != null) {
                    em.close();
                }

                throw new ClassModifiedException();
            } else {
                java.util.Set<History> historyListOld = persistentManagementStatus.getHistoryList();
                java.util.Set<History> historyListNew = managementStatus.getHistoryList();
                List<String> illegalOrphanMessages = null;
                for (History historyListOldHistory : historyListOld) {
                    if (!historyListNew.contains(historyListOldHistory)) {
                        if (illegalOrphanMessages == null) {
                            illegalOrphanMessages = new ArrayList<String>();
                        }
                        illegalOrphanMessages.add("You must retain ManagementHistory " + historyListOldHistory
                                + " since its fkIdManagementStatus field is not nullable.");
                    }
                }
                if (illegalOrphanMessages != null) {
                    throw new IllegalOrphanException(illegalOrphanMessages);
                }
                java.util.Set<History> attachedHistoryListNew = new java.util.HashSet<History>();
                for (History historyListNewHistoryToAttach : historyListNew) {
                    historyListNewHistoryToAttach = em.getReference(historyListNewHistoryToAttach.getClass(),
                            historyListNewHistoryToAttach.getIdHistory());
                    attachedHistoryListNew.add(historyListNewHistoryToAttach);
                }
                historyListNew = attachedHistoryListNew;
                managementStatus.setHistoryList(historyListNew);
                managementStatus = em.merge(managementStatus);
                for (History historyListNewHistory : historyListNew) {
                    if (!historyListOld.contains(historyListNewHistory)) {
                        ManagementStatus oldFkIdManagementStatusOfHistoryListNewHistory = historyListNewHistory
                                .getFkIdManagementStatus();
                        historyListNewHistory.setFkIdManagementStatus(managementStatus);
                        historyListNewHistory = em.merge(historyListNewHistory);
                        if (oldFkIdManagementStatusOfHistoryListNewHistory != null
                                && !oldFkIdManagementStatusOfHistoryListNewHistory.equals(managementStatus)) {
                            oldFkIdManagementStatusOfHistoryListNewHistory.getHistoryList()
                                    .remove(historyListNewHistory);
                            oldFkIdManagementStatusOfHistoryListNewHistory = em
                                    .merge(oldFkIdManagementStatusOfHistoryListNewHistory);
                        }
                    }
                }
                em.getTransaction().commit();

                result = Boolean.TRUE;
                if (em != null) {
                    em.close();
                }
            }
        } else {
            throw new ClassEliminatedException();
        }

        return result;
    }

    public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            ManagementStatus managementStatus;

            try {
                managementStatus = em.getReference(ManagementStatus.class, id);
                managementStatus.getIdManagementStatus();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The management status with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            java.util.Set<History> historyListOrphanCheck = managementStatus.getHistoryList();
            for (History historyListOrphanCheckHistory : historyListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This ManagementStatus (" + managementStatus
                        + ") cannot be destroyed since the ManagementHistory " + historyListOrphanCheckHistory
                        + " in its historyList field has a non-nullable fkIdManagementStatus field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(managementStatus);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    /**
     * Checks whether a management status with the given name is already stored.
     *
     * @param nameStatus the name of the management status
     * @return true when that status is already stored, false otherwise
     *
     */
    public boolean verifyManagementStatusExists(String nameStatus) {
        boolean result = false;

        List<ManagementStatus> managementStatuses = this.findManagementStatusEntities();

        for (Iterator<ManagementStatus> it = managementStatuses.iterator(); it.hasNext();) {
            ManagementStatus managementStatus = it.next();

            if (managementStatus.getName().equals(nameStatus)) {
                return true;
            }
        }
        return result;
    }

    public List<ManagementStatus> findManagementStatusEntities() {
        return findManagementStatusEntities(true, -1, -1);
    }

    public List<ManagementStatus> findManagementStatusEntities(int maxResults, int firstResult) {
        return findManagementStatusEntities(false, maxResults, firstResult);
    }

    private List<ManagementStatus> findManagementStatusEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createQuery("select object(o) from ManagementStatus as o");
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public ManagementStatus findManagementStatus(Integer id) {
        EntityManager em = getEntityManager();

        try {
            return em.find(ManagementStatus.class, id);
        } finally {
            em.close();
        }
    }

    public int getManagementStatusCount() {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createQuery("select count(o) from ManagementStatus as o");
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }

    @Override
    public String getNameJpa() {
        return this.getClass().getName();
    }
}
