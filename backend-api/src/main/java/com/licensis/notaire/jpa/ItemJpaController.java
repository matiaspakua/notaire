/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.licensis.notaire.jpa;

import com.licensis.notaire.jpa.exceptions.NonexistentEntityException;
import com.licensis.notaire.jpa.interfaz.IPersistenciaJpa;
import com.licensis.notaire.negocio.Item;
import com.licensis.notaire.negocio.Presupuesto;
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
public class ItemJpaController implements Serializable, IPersistenciaJpa
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
            Presupuesto fkIdPresupuesto = item.getFkIdPresupuesto();
            if (fkIdPresupuesto != null)
            {
                fkIdPresupuesto = em.getReference(fkIdPresupuesto.getClass(), fkIdPresupuesto.getIdPresupuesto());
                item.setFkIdPresupuesto(fkIdPresupuesto);
            }
            em.persist(item);
            if (fkIdPresupuesto != null)
            {
                fkIdPresupuesto.getItemList().add(item);
                fkIdPresupuesto = em.merge(fkIdPresupuesto);
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
        Boolean modificado = Boolean.FALSE;
        try
        {
            em = getEntityManager();
            em.getTransaction().begin();
            Item persistentItem = em.find(Item.class, item.getIdItem());
            Presupuesto fkIdPresupuestoOld = persistentItem.getFkIdPresupuesto();
            Presupuesto fkIdPresupuestoNew = item.getFkIdPresupuesto();
            if (fkIdPresupuestoNew != null)
            {
                fkIdPresupuestoNew = em.getReference(fkIdPresupuestoNew.getClass(), fkIdPresupuestoNew.getIdPresupuesto());
                item.setFkIdPresupuesto(fkIdPresupuestoNew);
            }
            item = em.merge(item);
            if (fkIdPresupuestoOld != null && !fkIdPresupuestoOld.equals(fkIdPresupuestoNew))
            {
                fkIdPresupuestoOld.getItemList().remove(item);
                fkIdPresupuestoOld = em.merge(fkIdPresupuestoOld);
            }
            if (fkIdPresupuestoNew != null && !fkIdPresupuestoNew.equals(fkIdPresupuestoOld))
            {
                fkIdPresupuestoNew.getItemList().add(item);
                fkIdPresupuestoNew = em.merge(fkIdPresupuestoNew);
            }
            em.getTransaction().commit();
            modificado = Boolean.TRUE;
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

        return modificado;
    }

    public Boolean destroy(Integer id) throws NonexistentEntityException
    {
        Boolean eliminado = false;
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
            Presupuesto fkIdPresupuesto = item.getFkIdPresupuesto();
            if (fkIdPresupuesto != null)
            {
                fkIdPresupuesto.getItemList().remove(item);
                fkIdPresupuesto = em.merge(fkIdPresupuesto);
            }
            em.remove(item);
            em.getTransaction().commit();
            eliminado = true;
        }
        finally
        {
            if (em != null)
            {
                em.close();
            }
        }
        return eliminado;
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

    public List<Item> findItemsPresupuesto(Integer pIdPresupuesto)
    {
        EntityManager em = getEntityManager();

        List<Item> misItems = null;

        try
        {
            // Nombre del @NamedQuery definido en nuestra clase
            Query q = em.createNamedQuery("Item.findByPresupuesto");

            //Le paso el nombre del parametro del query, y el valor a buscar.
            q.setParameter("idPresupuesto", pIdPresupuesto);

            misItems = (List<Item>) q.getResultList();

            return misItems;

        }
        finally
        {
            em.close();
        }
    }

    @Override
    public String getNombreJpa()
    {
        return this.getClass().getName();
    }

    public Boolean eliminarItem(Item miItem)
    {

        Boolean eliminado = false;
        int deleted = 0;

        EntityManager em = this.getEntityManager();
        EntityTransaction tx = null;

        // Abro una nueva transaccion
        tx = em.getTransaction();
        tx.begin();

        // Creo el query
        Query query = em.createQuery("DELETE FROM Item i WHERE i.idItem = ?1");

        //Seteo el parametro 1
        query.setParameter(1, miItem.getIdItem());

        // Ejecuto el query
        deleted = query.executeUpdate();

        // Hago commit
        tx.commit();

        em.close();

        if (deleted > 0)
        {
            eliminado = true;
        }

        return eliminado;
    }
}
