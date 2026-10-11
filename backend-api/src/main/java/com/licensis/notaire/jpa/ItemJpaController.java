/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.jpa;

import com.licensis.notaire.jpa.exceptions.NonexistentEntityException;
import com.licensis.notaire.jpa.JpaPersistence;
import com.licensis.notaire.business.Item;
import com.licensis.notaire.business.Budget;
import java.io.Serializable;
import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;
import jakarta.transaction.UserTransaction;

/**
 *
 * @author juanca
 */
public class ItemJpaController implements Serializable, JpaPersistence
{

    public ItemJpaController(UserTransaction utx, EntityManagerFactory emf)
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

    public int create(Item item)
    {
        int id = -1;
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            Budget fkIdBudget = item.getFkIdBudget();
            if (fkIdBudget != null)
            {
                fkIdBudget = em.getReference(fkIdBudget.getClass(), fkIdBudget.getIdBudget());
                item.setFkIdBudget(fkIdBudget);
            }
            em.persist(item);
            if (fkIdBudget != null)
            {
                fkIdBudget.getItemList().add(item);
                fkIdBudget = em.merge(fkIdBudget);
            }
            em.getTransaction().commit();
            id = item.getIdItem();
        }
        finally
        {
            if (em != null)
            {
                em.close();
            }
        }
        return id;
    }

    public Boolean edit(Item item) throws NonexistentEntityException, Exception
    {
        EntityManager em = null;
        Boolean modified = Boolean.FALSE;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            Item persistentItem = em.find(Item.class, item.getIdItem());
            Budget fkIdBudgetOld = persistentItem.getFkIdBudget();
            Budget fkIdBudgetNew = item.getFkIdBudget();
            if (fkIdBudgetNew != null)
            {
                fkIdBudgetNew = em.getReference(fkIdBudgetNew.getClass(), fkIdBudgetNew.getIdBudget());
                item.setFkIdBudget(fkIdBudgetNew);
            }
            item = em.merge(item);
            if (fkIdBudgetOld != null && !fkIdBudgetOld.equals(fkIdBudgetNew))
            {
                fkIdBudgetOld.getItemList().remove(item);
                fkIdBudgetOld = em.merge(fkIdBudgetOld);
            }
            if (fkIdBudgetNew != null && !fkIdBudgetNew.equals(fkIdBudgetOld))
            {
                fkIdBudgetNew.getItemList().add(item);
                fkIdBudgetNew = em.merge(fkIdBudgetNew);
            }
            em.getTransaction().commit();
            modified = Boolean.TRUE;
        }
        catch (Exception ex)
        {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0)
            {
                Integer id = item.getIdItem();
                if (findItem(id) == null)
                {
                    throw new NonexistentEntityException("The item with id " + id + " no longer exists.");
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

        return modified;
    }

    public Boolean destroy(Integer id) throws NonexistentEntityException
    {
        Boolean removed = false;
        EntityManager em = null;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            Item item;
            try
            {
                item = em.getReference(Item.class, id);
                item.getIdItem();
            }
            catch (EntityNotFoundException enfe)
            {
                throw new NonexistentEntityException("The item with id " + id + " no longer exists.", enfe);
            }
            Budget fkIdBudget = item.getFkIdBudget();
            if (fkIdBudget != null)
            {
                fkIdBudget.getItemList().remove(item);
                fkIdBudget = em.merge(fkIdBudget);
            }
            em.remove(item);
            em.getTransaction().commit();
            removed = true;
        }
        finally
        {
            if (em != null)
            {
                em.close();
            }
        }
        return removed;
    }

    public List<Item> findItemEntities()
    {
        return findItemEntities(true, -1, -1);
    }

    public List<Item> findItemEntities(int maxResults, int firstResult)
    {
        return findItemEntities(false, maxResults, firstResult);
    }

    private List<Item> findItemEntities(boolean all, int maxResults, int firstResult)
    {
        EntityManager em = getEntityManager();
        try
        {
            Query q = em.createQuery("select object(o) from Item as o");
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

    public Item findItem(Integer id)
    {
        EntityManager em = getEntityManager();
        try
        {
            return em.find(Item.class, id);
        }
        finally
        {
            em.close();
        }
    }

    public int getItemCount()
    {
        EntityManager em = getEntityManager();
        try
        {
            Query q = em.createQuery("select count(o) from Item as o");
            return ((Long) q.getSingleResult()).intValue();
        }
        finally
        {
            em.close();
        }
    }

    public List<Item> findItemsBudget(Integer pIdBudget)
    {
        EntityManager em = getEntityManager();

        List<Item> items = null;

        try
        {
            // Named query declared on the entity.
            Query q = em.createNamedQuery("Item.findByBudget");

            // Bind the named query parameter.
            q.setParameter("idBudget", pIdBudget);

            items = (List<Item>) q.getResultList();

            return items;

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

    public Boolean deleteItem(Item item)
    {

        Boolean removed = false;
        int deleted = 0;

        EntityManager em = this.getEntityManager();
        EntityTransaction tx = null;

        // Open a transaction.
        tx = em.getTransaction();
        tx.begin();

        // Build the query.
        Query query = em.createQuery("DELETE FROM Item i WHERE i.idItem = ?1");

        // Bind parameter 1.
        query.setParameter(1, item.getIdItem());

        // Run the query.
        deleted = query.executeUpdate();

        // Commit the transaction.
        tx.commit();

        em.close();

        if (deleted > 0)
        {
            removed = true;
        }

        return removed;
    }
}
