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
import com.licensis.notaire.jpa.interfaz.IPersistenciaJpa;
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
public class ManagementStatusJpaController implements Serializable, IPersistenciaJpa {

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

            if (this.verificarExistenciaManagementStatus(managementStatus.getName())) {
                throw new PreexistingEntityException("La entidad ya existe");
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
        Integer version = ConstantesPersistencia.VersionINICIAL;
        Integer oldVersion = ConstantesPersistencia.VersionINICIAL;
        Boolean resultado = Boolean.FALSE;
        EntityManager em = null;

        em = getEntityManager();
        em.getTransaction().begin();
        ManagementStatus persistentManagementStatus = em.find(ManagementStatus.class,
                managementStatus.getIdManagementStatus());

        if (persistentManagementStatus != null) {
            version = persistentManagementStatus.getVersion();
            oldVersion = managementStatus.getVersion();

            if (version != oldVersion) // Si son distintas "Alguien modifico o elimino el objeto"
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
                        illegalOrphanMessages.add("You must retain Historial " + historyListOldHistory
                                + " since its fkIdEstadoGestion field is not nullable.");
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

                resultado = Boolean.TRUE;
                if (em != null) {
                    em.close();
                }
            }
        } else {
            throw new ClassEliminatedException();
        }

        return resultado;
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
                throw new NonexistentEntityException("The estadoDeGestion with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            java.util.Set<History> historyListOrphanCheck = managementStatus.getHistoryList();
            for (History historyListOrphanCheckHistory : historyListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This EstadoDeGestion (" + managementStatus
                        + ") cannot be destroyed since the Historial " + historyListOrphanCheckHistory
                        + " in its historialList field has a non-nullable fkIdEstadoGestion field.");
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
     * Verifica si el estado de gestion indicado ya se encuentra registrado o no.
     *
     * @param nombreEstado El nombre del estado de gestion.
     * @return Verdadero si es estado de gestion ya se encuentra registrado, falso
     *         en caso
     *         contrario.
     *
     */
    public boolean verificarExistenciaManagementStatus(String nameStatus) {
        boolean resultado = false;

        List<ManagementStatus> listaEstadosDeGestiones = this.findManagementStatusEntities();

        for (Iterator<ManagementStatus> it = listaEstadosDeGestiones.iterator(); it.hasNext();) {
            ManagementStatus managementStatus = it.next();

            if (managementStatus.getName().equals(nameStatus)) {
                return true;
            }
        }
        return resultado;
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
            Query q = em.createQuery("select object(o) from EstadoDeGestion as o");
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
            Query q = em.createQuery("select count(o) from EstadoDeGestion as o");
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
