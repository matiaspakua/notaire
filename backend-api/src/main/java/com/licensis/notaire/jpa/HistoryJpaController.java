/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.jpa;

import com.licensis.notaire.jpa.exceptions.CreateEntityException;
import com.licensis.notaire.jpa.exceptions.NonexistentEntityException;
import com.licensis.notaire.jpa.JpaPersistence;
import com.licensis.notaire.business.BusinessConstants;
import com.licensis.notaire.business.ManagementStatus;
import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.History;
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
public class HistoryJpaController implements Serializable, JpaPersistence
{

    public HistoryJpaController(UserTransaction utx, EntityManagerFactory emf)
    {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager()
    {
        return emf.createEntityManager();
    }

    /**
     * Creates a history record.
     *
     * @param history the history record to persist
     * @return the new history id, or {@link BusinessConstants#ID_OBJETO_NO_VALIDO} when creation fails
     */
    public Integer create(History history) throws CreateEntityException
    {
        Integer idHistory = BusinessConstants.ID_OBJETO_NO_VALIDO;
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            ManagementStatus fkIdManagementStatus = history.getFkIdManagementStatus();
            if (fkIdManagementStatus != null)
            {
                fkIdManagementStatus = em.getReference(fkIdManagementStatus.getClass(), fkIdManagementStatus.getIdManagementStatus());
                history.setFkIdManagementStatus(fkIdManagementStatus);
            }
            DeedManagement fkIdManagement = history.getFkIdManagement();
            if (fkIdManagement != null)
            {
                fkIdManagement = em.getReference(fkIdManagement.getClass(), fkIdManagement.getIdManagement());
                history.setFkIdManagement(fkIdManagement);
            }
            em.persist(history);
            if (fkIdManagementStatus != null)
            {
                fkIdManagementStatus.getHistoryList().add(history);
                fkIdManagementStatus = em.merge(fkIdManagementStatus);
            }
            if (fkIdManagement != null)
            {
                fkIdManagement.getHistoryList().add(history);
                fkIdManagement = em.merge(fkIdManagement);
            }
            em.getTransaction().commit();
            idHistory = history.getIdHistory();
        }
        catch (Exception ex)
        {
            throw new CreateEntityException("Not pudo create a entidad tipo: ManagementHistory");
        }
        finally
        {
            if (em != null)
            {
                em.close();
            }
        }
        return idHistory;
    }

    public void edit(History history) throws NonexistentEntityException, Exception
    {
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            History persistentHistory = em.find(History.class, history.getIdHistory());
            ManagementStatus fkIdManagementStatusOld = persistentHistory.getFkIdManagementStatus();
            ManagementStatus fkIdManagementStatusNew = history.getFkIdManagementStatus();
            DeedManagement fkIdManagementOld = persistentHistory.getFkIdManagement();
            DeedManagement fkIdManagementNew = history.getFkIdManagement();
            if (fkIdManagementStatusNew != null)
            {
                fkIdManagementStatusNew = em.getReference(fkIdManagementStatusNew.getClass(), fkIdManagementStatusNew.getIdManagementStatus());
                history.setFkIdManagementStatus(fkIdManagementStatusNew);
            }
            if (fkIdManagementNew != null)
            {
                fkIdManagementNew = em.getReference(fkIdManagementNew.getClass(), fkIdManagementNew.getIdManagement());
                history.setFkIdManagement(fkIdManagementNew);
            }
            history = em.merge(history);
            if (fkIdManagementStatusOld != null && !fkIdManagementStatusOld.equals(fkIdManagementStatusNew))
            {
                fkIdManagementStatusOld.getHistoryList().remove(history);
                fkIdManagementStatusOld = em.merge(fkIdManagementStatusOld);
            }
            if (fkIdManagementStatusNew != null && !fkIdManagementStatusNew.equals(fkIdManagementStatusOld))
            {
                fkIdManagementStatusNew.getHistoryList().add(history);
                fkIdManagementStatusNew = em.merge(fkIdManagementStatusNew);
            }
            if (fkIdManagementOld != null && !fkIdManagementOld.equals(fkIdManagementNew))
            {
                fkIdManagementOld.getHistoryList().remove(history);
                fkIdManagementOld = em.merge(fkIdManagementOld);
            }
            if (fkIdManagementNew != null && !fkIdManagementNew.equals(fkIdManagementOld))
            {
                fkIdManagementNew.getHistoryList().add(history);
                fkIdManagementNew = em.merge(fkIdManagementNew);
            }
            em.getTransaction().commit();
        }
        catch (Exception ex)
        {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0)
            {
                Integer id = history.getIdHistory();
                if (findHistory(id) == null)
                {
                    throw new NonexistentEntityException("The history with id " + id + " no longer exists.");
                }
            }
            throw ex;
        }
        finally
        {
            if (em != null)
            {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws NonexistentEntityException
    {
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            History history;
            try
            {
                history = em.getReference(History.class, id);
                history.getIdHistory();
            }
            catch (EntityNotFoundException enfe)
            {
                throw new NonexistentEntityException("The history with id " + id + " no longer exists.", enfe);
            }
            ManagementStatus fkIdManagementStatus = history.getFkIdManagementStatus();
            if (fkIdManagementStatus != null)
            {
                fkIdManagementStatus.getHistoryList().remove(history);
                fkIdManagementStatus = em.merge(fkIdManagementStatus);
            }
            DeedManagement fkIdManagement = history.getFkIdManagement();
            if (fkIdManagement != null)
            {
                fkIdManagement.getHistoryList().remove(history);
                fkIdManagement = em.merge(fkIdManagement);
            }
            em.remove(history);
            em.getTransaction().commit();
        }
        finally
        {
            if (em != null)
            {
                em.close();
            }
        }
    }

    /**
     * Returns the current status of one management.
     *
     * @param idManagement the management id
     * @return a history row with the management's current status
     */
    public History findCurrentManagementStatus(Integer idManagement)
    {
        History currentManagementStatus = new History();

        EntityManager em = getEntityManager();

        Query q = em.createNamedQuery("History.currentManagementStatus");
        q.setParameter("fkIdManagement", idManagement);

        currentManagementStatus = (History) q.getSingleResult();

        return currentManagementStatus;
    }

    /**
     * Returns every history record for one management.
     *
     * @param idManagement the management id
     * @return the status changes for that management
     */
    public List<History> findHistoryRecords(Integer idManagement)
    {
        List<History> recordHistory = new ArrayList<History>();

        EntityManager em = getEntityManager();

        Query q = em.createNamedQuery("History.findByIdManagement");
        q.setParameter("idManagement", idManagement);

        recordHistory = q.getResultList();

        return recordHistory;
    }

    public List<History> findHistoryEntities()
    {
        return findHistoryEntities(true, -1, -1);
    }

    public List<History> findHistoryEntities(int maxResults, int firstResult)
    {
        return findHistoryEntities(false, maxResults, firstResult);
    }

    private List<History> findHistoryEntities(boolean all, int maxResults, int firstResult)
    {
        EntityManager em = getEntityManager();
        try
        {
            Query q = em.createQuery("select object(o) from History as o");
            if (!all)
            {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        }
        finally
        {
            em.close();
        }
    }

    public History findHistory(Integer id)
    {
        EntityManager em = getEntityManager();
        try
        {
            return em.find(History.class, id);
        }
        finally
        {
            em.close();
        }
    }

    public int getHistoryCount()
    {
        EntityManager em = getEntityManager();
        try
        {
            Query q = em.createQuery("select count(o) from History as o");
            return ((Long) q.getSingleResult()).intValue();
        }
        finally
        {
            em.close();
        }
    }

    @Override
    public String getNameJpa()
    {
        return this.getClass().getName();
    }
}
